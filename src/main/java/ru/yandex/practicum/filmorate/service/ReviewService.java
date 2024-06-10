package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;

    public Review createReview(Review review) {
        try {
            long id = reviewStorage.createReview(review);
            review.setReviewId(id);
            return review;
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("FOREIGN KEY(user_id)")) {
                throw new NotFoundException("Пользователь с таким идентификатором не существует.");
            } else if (e.getMessage().contains("FOREIGN KEY(film_id)")) {
                throw new NotFoundException("Фильм с таким идентификатором не существует.");
            } else {
                throw e;
            }
        }
    }

    public Review updateReview(Review review) {
        try {
            reviewStorage.updateReview(review);
            /*
                реализован второй запрос так как есть поле useful, которое зависит от состояния другой таблицы
            */
            return getReview(review.getReviewId());
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("FOREIGN KEY(user_id)")) {
                throw new NotFoundException("Пользователь с таким идентификатором не существует.");
            } else if (e.getMessage().contains("FOREIGN KEY(film_id)")) {
                throw new NotFoundException("Фильм с таким идентификатором не существует.");
            } else {
                throw e;
            }
        }
    }

    public boolean deleteReview(Long id) {
        return reviewStorage.deleteReview(id);
    }

    public Review getReview(Long id) {
        try {
            return reviewStorage.getReview(id).orElse(null);
        } catch (Exception e) {
            throw new NotFoundException("Review not found.");
        }
    }

    // НЕ РАБОТАЕТ !!!
    public List<Review> getReviews(Long filmId, Integer count) {
        if (count == null || count <= 0) count = 10;
        if (filmId != null) {
            return reviewStorage.getReviewsForFilm(filmId, count);
        }
        return reviewStorage.getNReviewsForEachFilm(count);
    }

    public Review likeReview(Long reviewId, Long userId) {
        reviewStorage.setLike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review dislikeReview(Long reviewId, Long userId) {
        reviewStorage.setDislike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review deleteLike(Long reviewId, Long userId) {
        reviewStorage.removeLike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review deleteDislike(Long reviewId, Long userId) {
        reviewStorage.removeDislike(reviewId, userId);
        return getReview(reviewId);
    }
}
