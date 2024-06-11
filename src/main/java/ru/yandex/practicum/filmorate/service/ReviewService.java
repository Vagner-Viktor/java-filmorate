package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    public Review createReview(Review review) {
        if (!userStorage.checkUserExists(review.getUserId()))
            throw new NotFoundException("Пользователь с таким id не существует.");
        if (!filmStorage.checkFilmExists(review.getFilmId()))
            throw new NotFoundException("Фильм с таким id не существует.");

        long id = reviewStorage.createReview(review);
        review.setReviewId(id);
        return review;
    }

    public Review updateReview(Review review) {
        if (!reviewStorage.checkReviewExists(review.getReviewId()))
            throw new NotFoundException("Ревью с таким id не существует.");
        if (!userStorage.checkUserExists(review.getUserId()))
            throw new NotFoundException("Пользователь с таким id не существует.");
        if (!filmStorage.checkFilmExists(review.getFilmId()))
            throw new NotFoundException("Фильм с таким id не существует.");

        reviewStorage.updateReview(review);
            /*
                реализован второй запрос так как есть поле useful, которое зависит от состояния другой таблицы
            */
        return getReview(review.getReviewId());
    }

    public boolean deleteReview(Long reviewId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException("Ревью с таким id не существует.");

        return reviewStorage.deleteReview(reviewId);
    }

    public Review getReview(Long reviewId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException("Ревью с таким id не существует.");

        return reviewStorage.getReview(reviewId).orElse(null);
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        if (count == null || count <= 0) count = 10;
        if (filmId != null) {
            if (!filmStorage.checkFilmExists(filmId))
                throw new NotFoundException("Фильм с таким id не существует.");

            return reviewStorage.getReviewsForFilm(filmId, count);
        }
        return reviewStorage.getNReviewsForEachFilm(count);
    }

    public Review likeReview(Long reviewId, Long userId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException("Ревью с таким id не существует.");
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException("Пользователь с таким id не существует.");

        reviewStorage.setLike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review dislikeReview(Long reviewId, Long userId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException("Ревью с таким id не существует.");
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException("Пользователь с таким id не существует.");

        reviewStorage.setDislike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review deleteLike(Long reviewId, Long userId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException("Ревью с таким id не существует.");
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException("Пользователь с таким id не существует.");

        reviewStorage.removeLike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review deleteDislike(Long reviewId, Long userId) {
        if (!reviewStorage.checkReviewExists(reviewId))
            throw new NotFoundException("Ревью с таким id не существует.");
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException("Пользователь с таким id не существует.");

        reviewStorage.removeDislike(reviewId, userId);
        return getReview(reviewId);
    }
}
