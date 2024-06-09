package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Primary
public class ReviewDbStorage extends BaseDbStorage<Review> implements ReviewStorage{

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    private static final String REQUEST_ADD_REVIEW = """
                INSERT INTO "reviews" ("film_id", "user_id", "content", "feedback_id")
                VALUES (?, ?, ?, (
                    SELECT "feedback_id"
                    FROM "feedbacks"
                    WHERE "feedback" = ? )
                    );
                """;

    private static final String REQUEST_UPDATE_REVIEW = """
             UPDATE "reviews"
             SET "film_id" = ?, "user_id" = ?, "content" = ?, "feedback_id" = ?
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
                    f."feedback" AS feedback,
                    SUM(u."weigh") AS evaluation
                FROM "reviews" AS r
                LEFT JOIN "feedbacks" AS f ON r."feedback_id" = f."feedback_id"
                LEFT JOIN "usability_reviews" AS ur ON r."review_id" = ur."review_id"
                LEFT JOIN "usabilitys" AS u ON ur."usability_id" = u."usability_id"
                WHERE r."review_id" = ?
                """;

    private static final String REQUEST_GET_ALL_REVIEWS_FOR_FILM = """
            SELECT
                r."review_id" AS review_id,
                r."film_id" AS film_id,
                r."user_id" AS user_id,
                r."content" AS content,
                f."feedback" AS feedback,
                SUM(u."weigh") AS evaluation
            FROM "reviews" AS r
            LEFT JOIN "feedbacks" AS f ON r."feedback_id" = f."feedback_id"
            LEFT JOIN "usability_reviews" AS ur ON r."review_id" = ur."review_id"
            LEFT JOIN "usabilitys" AS u ON ur."usability_id" = u."usability_id"
            WHERE r."film_id" = ?
            ORDER BY r."review_id"
            LIMIT ?
            """;

    private static final String REQUEST_GET_ALL_REVIEWS_FOR_ALL_FILMS = """
            SELECT
                r."review_id" AS review_id,
                r."film_id" AS film_id,
                r."user_id" AS user_id,
                r."content" AS content,
                f."feedback" AS feedback,
                SUM(u."weigh") AS evaluation
            FROM "reviews" AS r
            LEFT JOIN "feedbacks" AS f ON r."feedback_id" = f."feedback_id"
            LEFT JOIN "usability_reviews" AS ur ON r."review_id" = ur."review_id"
            LEFT JOIN "usabilitys" AS u ON ur."usability_id" = u."usability_id"
            ORDER BY r."review_id", r."film_id"
            LIMIT ?
            """;

    private static final String REQUEST_SET_LIKE = """
            INSERT INTO "usability_reviews" ("film_id", "user_id", "usability_id")
            VALUES (?, ?, 1)
            """;

    private static final String REQUEST_SET_DISLIKE = """
            INSERT INTO "usability_reviews" ("film_id", "user_id", "usability_id")
            VALUES (?, ?, 2)
            """;

    private static final String REQUEST_REMOVE_LIKE = """
            DELETE FROM "usability_reviews"
            WHERE "user_id" = ? AND "review_id" = ? AND "usability_id" = 1;
            """;

    private static final String REQUEST_REMOVE_DISLIKE = """
            DELETE FROM "usability_reviews"
            WHERE "user_id" = ? AND "review_id" = ? AND "usability_id" = 1;
            """;

    @Override
    public long createReview(Review review) {
        return insertGetKey(REQUEST_ADD_REVIEW,
                review.getFilmId(),
                review.getUserId(),
                review.getContent(),
                review.getFeedback().toString());
    }

    @Override
    public void updateReview(Review review) {
        update(REQUEST_UPDATE_REVIEW,
                review.getFilmId(),
                review.getUserId(),
                review.getContent(),
                review.getFeedback().toString(),
                review.getReviewId());
    }

    @Override
    public boolean deleteReview(Integer id) {
        return delete(REQUEST_DELETE_REVIEW, id);
    }

    @Override
    public Optional<Review> getReview(Integer id) {
        return findOne(REQUEST_GET_REVIEW, id);
    }

    @Override
    public List<Review> getReviewsForFilm(Long filmId, Integer count) {
        return findMany(REQUEST_GET_ALL_REVIEWS_FOR_FILM, filmId, count);
    }

    @Override
    public List<Review> getNReviewsForEachFilm(Integer count) {
        return List.of();
    }

    @Override
    public void setLike(Integer reviewId, Integer userId) {
        jdbc.update(REQUEST_SET_LIKE);
    }

    @Override
    public void setDislike(Integer reviewId, Integer userId) {
        jdbc.update(REQUEST_SET_DISLIKE);
    }

    @Override
    public void removeLike(Integer reviewId, Integer userId) {
        jdbc.update(REQUEST_REMOVE_LIKE);
    }

    @Override
    public void removeDislike(Integer reviewId, Integer userId) {
        jdbc.update(REQUEST_REMOVE_DISLIKE);
    }


}
