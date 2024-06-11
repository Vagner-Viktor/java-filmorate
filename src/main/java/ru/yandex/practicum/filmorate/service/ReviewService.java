package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private static final String NOT_FOUND_REVIEW_MESSAGE = "Ревью с таким id не существует.";
    private static final String NOT_FOUND_USER_MESSAGE = "Пользователь с таким id не существует.";
    private static final String NOT_FOUND_FILM_MESSAGE = "Фильм с таким id не существует.";

    public Review createReview(Review review) {
        if (!userStorage.checkUserExists(review.getUserId()))
            throw new NotFoundException(NOT_FOUND_USER_MESSAGE);
        if (!filmStorage.checkFilmExists(review.getFilmId()))
            throw new NotFoundException(NOT_FOUND_FILM_MESSAGE);
        long id = reviewStorage.createReview(review);
        review.setReviewId(id);
        return review;
    }

    public Review updateReview(Review review) {
        if (!reviewStorage.checkReviewExists(review.getReviewId()))
            throw new NotFoundException(NOT_FOUND_REVIEW_MESSAGE);
        if (!userStorage.checkUserExists(review.getUserId()))
            throw new NotFoundException(NOT_FOUND_USER_MESSAGE);
        if (!filmStorage.checkFilmExists(review.getFilmId()))
            throw new NotFoundException(NOT_FOUND_FILM_MESSAGE);
        reviewStorage.updateReview(review);
            /*
                реализован второй запрос так как есть поле useful, которое зависит от состояния другой таблицы
            */
        return getReview(review.getReviewId());
    }

    public boolean deleteReview(Long reviewId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException(NOT_FOUND_REVIEW_MESSAGE);
        return reviewStorage.deleteReview(reviewId);
    }

    public Review getReview(Long reviewId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException(NOT_FOUND_REVIEW_MESSAGE);
        return reviewStorage.getReview(reviewId).orElse(null);
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        if (count == null || count <= 0) count = 10;
        if (filmId != null) {
            if (!filmStorage.checkFilmExists(filmId))
                throw new NotFoundException(NOT_FOUND_FILM_MESSAGE);
            return reviewStorage.getReviewsForFilm(filmId, count);
        }
        return reviewStorage.getNReviewsForEachFilm(count);
    }

    public Review likeReview(Long reviewId, Long userId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException(NOT_FOUND_REVIEW_MESSAGE);
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException(NOT_FOUND_USER_MESSAGE);
        reviewStorage.setLike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review dislikeReview(Long reviewId, Long userId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException(NOT_FOUND_REVIEW_MESSAGE);
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException(NOT_FOUND_USER_MESSAGE);
        reviewStorage.setDislike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review deleteLike(Long reviewId, Long userId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException(NOT_FOUND_REVIEW_MESSAGE);
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException(NOT_FOUND_USER_MESSAGE);
        reviewStorage.removeLike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review deleteDislike(Long reviewId, Long userId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException(NOT_FOUND_REVIEW_MESSAGE);
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException(NOT_FOUND_USER_MESSAGE);
        reviewStorage.removeDislike(reviewId, userId);
        return getReview(reviewId);
    }
}
