package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    long createReview(Review review);

    void updateReview(Review review);

    boolean deleteReview(Long id);

    Optional<Review> getReview(Long id);

    List<Review> getReviewsForFilm(Long filmId, Integer count);

    List<Review> getNReviewsForEachFilm(Integer count);

    void setLike(Long reviewId, Long userId);

    void updateLike(Long reviewId, Long userId);

    void removeLike(Long reviewId, Long userId);

    void setDislike(Long reviewId, Long userId);

    void updateDislike(Long reviewId, Long userId);

    void removeDislike(Long reviewId, Long userId);

    boolean isReviewExists(Long id);
}
