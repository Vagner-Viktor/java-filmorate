package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Primary
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage {

    private final UsabilityStateStorage usabilityStateStorage;
    private final UserFeedStorage userFeedStorage;

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper, UsabilityStateStorage usabilityStateStorage, UserFeedStorage userFeedStorage) {
        super(jdbc, mapper);
        this.usabilityStateStorage = usabilityStateStorage;
        this.userFeedStorage = userFeedStorage;
    }

    private static final String REQUEST_ADD_REVIEW = """
            INSERT INTO "reviews" ("film_id", "user_id", "content", "is_positive")
            VALUES (?, ?, ?, ?);
            """;

    private static final String REQUEST_UPDATE_REVIEW = """
            UPDATE "reviews"
            SET "content" = ?, "is_positive" = ?
            WHERE "review_id" = ?;
            """;

    private static final String REQUEST_DELETE_REVIEW = """
            DELETE FROM "reviews"
            WHERE "review_id" = ?;
            """;

    private static final String REQUEST_GET_REVIEW = """
            SELECT
                r."review_id" AS review_id,
                r."film_id" AS film_id,
                r."user_id" AS user_id,
                r."content" AS content,
                r."is_positive" AS is_positive,
                SUM(u."weigh") AS useful
            FROM "reviews" AS r
            LEFT JOIN "usability_reviews" AS ur ON r."review_id" = ur."review_id"
            LEFT JOIN "usabilitys" AS u ON ur."usability_id" = u."usability_id"
            WHERE r."review_id" = ?
            GROUP BY r."review_id";
            """;

    private static final String REQUEST_GET_ALL_REVIEWS_FOR_FILM = """
            SELECT
                r."review_id" AS review_id,
                r."film_id" AS film_id,
                r."user_id" AS user_id,
                r."content" AS content,
                r."is_positive" AS is_positive,
                SUM(u."weigh") AS useful
            FROM "reviews" AS r
            LEFT JOIN "usability_reviews" AS ur ON r."review_id" = ur."review_id"
            LEFT JOIN "usabilitys" AS u ON ur."usability_id" = u."usability_id"
            WHERE r."film_id" = ?
            GROUP BY r."review_id"
            ORDER BY r."review_id"
            LIMIT ?;
            """;

    private static final String REQUEST_GET_ALL_REVIEWS_FOR_ALL_FILMS = """
            WITH RankedReviews AS (
                SELECT
                    "review_id" AS review_id,
                    "film_id" AS film_id,
                    "user_id" AS user_id,
                    "content" AS content,
                    "is_positive" AS is_positive,
                    ROW_NUMBER() OVER (PARTITION BY "film_id" ORDER BY "review_id") AS rn
                FROM "reviews"
            )
            SELECT rr.*, COALESCE(SUM(u."weigh"), 0) AS useful
            FROM RankedReviews AS rr
            LEFT JOIN "usability_reviews" AS ur ON rr.review_id = ur."review_id"
            LEFT JOIN "usabilitys" AS u ON ur."usability_id" = u."usability_id"
            WHERE rn <= ? -- типо LIMIT
            GROUP BY rr.review_id
            ORDER BY useful DESC, rr.film_id, rn;
            """;

    private static final String REQUEST_SET_LIKE = """
            INSERT INTO "usability_reviews" ("user_id", "review_id", "usability_id")
            VALUES (?, ?, 1);
            """;

    private static final String REQUEST_SET_DISLIKE = """
            INSERT INTO "usability_reviews" ("user_id", "review_id", "usability_id")
            VALUES (?, ?, 2);
            """;

    private static final String REQUEST_UPDATE_TO_LIKE = """
            UPDATE "usability_reviews"
            SET "usability_id" = 1
            WHERE "user_id" = ? AND "review_id" = ?;
            """;

    private static final String REQUEST_UPDATE_TO_DISLIKE = """
            UPDATE "usability_reviews"
            SET "usability_id" = 2
            WHERE "user_id" = ? AND "review_id" = ?;
            """;

    private static final String REQUEST_REMOVE_LIKE = """
            DELETE FROM "usability_reviews"
            WHERE "user_id" = ? AND "review_id" = ? AND "usability_id" = 1;
            """;

    private static final String REQUEST_REMOVE_DISLIKE = """
            DELETE FROM "usability_reviews"
            WHERE "user_id" = ? AND "review_id" = ? AND "usability_id" = -1;
            """;

    @Override
    public long createReview(Review review) {
        long id = insertGetKey(REQUEST_ADD_REVIEW,
                review.getFilmId(),
                review.getUserId(),
                review.getContent(),
                review.getIsPositive());
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(review.getUserId())
                .entityId(id)
                .timestamp(Instant.now())
                .eventType(EventType.REVIEW.name())
                .operation(OperationType.ADD.name())
                .build());
        return id;
    }

    @Override
    public void updateReview(Review review) {
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(review.getUserId())
                .entityId(review.getReviewId())
                .timestamp(Instant.now())
                .eventType(EventType.REVIEW.name())
                .operation(OperationType.UPDATE.name())
                .build());
        update(REQUEST_UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
    }

    @Override
    public boolean deleteReview(Long id) {
        Review review = findOne(REQUEST_GET_REVIEW, id).orElse(null);
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(review.getUserId())
                .entityId(review.getReviewId())
                .timestamp(Instant.now())
                .eventType(EventType.REVIEW.name())
                .operation(OperationType.REMOVE.name())
                .build());
        return delete(REQUEST_DELETE_REVIEW, id);
    }

    @Override
    public Optional<Review> getReview(Long id) {
        return findOne(REQUEST_GET_REVIEW, id);
    }

    @Override
    public List<Review> getReviewsForFilm(Long filmId, Integer count) {
        return findMany(REQUEST_GET_ALL_REVIEWS_FOR_FILM, filmId, count);
    }

    @Override
    public List<Review> getNReviewsForEachFilm(Integer count) {
        return findMany(REQUEST_GET_ALL_REVIEWS_FOR_ALL_FILMS, count);
    }

    @Override
    public void setLike(Long reviewId, Long userId) {
        Integer state = usabilityStateStorage.getCurrentState(reviewId, userId).orElse(0);
        if (state == 0) {
            insert(REQUEST_SET_LIKE, userId, reviewId);
            userFeedStorage.create(UserFeed.builder()
                    .eventId(null)
                    .userId(userId)
                    .entityId(reviewId)
                    .timestamp(Instant.now())
                    .eventType(EventType.LIKE.name())
                    .operation(OperationType.ADD.name())
                    .build());
        } else if (state == -1) {
            update(REQUEST_UPDATE_TO_LIKE, userId, reviewId);
            userFeedStorage.create(UserFeed.builder()
                    .eventId(null)
                    .userId(userId)
                    .entityId(reviewId)
                    .timestamp(Instant.now())
                    .eventType(EventType.LIKE.name())
                    .operation(OperationType.UPDATE.name())
                    .build());
        }
    }

    @Override
    public void setDislike(Long reviewId, Long userId) {
        Integer state = usabilityStateStorage.getCurrentState(reviewId, userId).orElse(0);
        if (state == 0) {
            insert(REQUEST_SET_DISLIKE, userId, reviewId);
        } else if (state == 1) {
            update(REQUEST_UPDATE_TO_DISLIKE, userId, reviewId);
        }
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(userId)
                .entityId(reviewId)
                .timestamp(Instant.now())
                .eventType(EventType.REVIEW.name())
                .operation(OperationType.UPDATE.name())
                .build());
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        update(REQUEST_REMOVE_LIKE, userId, reviewId);
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(userId)
                .entityId(reviewId)
                .timestamp(Instant.now())
                .eventType(EventType.REVIEW.name())
                .operation(OperationType.REMOVE.name())
                .build());
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        update(REQUEST_REMOVE_DISLIKE, userId, reviewId);
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(userId)
                .entityId(reviewId)
                .timestamp(Instant.now())
                .eventType(EventType.REVIEW.name())
                .operation(OperationType.REMOVE.name())
                .build());
    }

    @Override
    public boolean checkReviewExists(Long id) {
        return getReview(id).isPresent();
    }


}
