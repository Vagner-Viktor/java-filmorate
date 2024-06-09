package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {


    long createReview(Review review);

    void updateReview(Review review);

    boolean deleteReview(Integer id);

    Optional<Review> getReview(Integer id);

    List<Review> getReviewsForFilm(Long filmId, Integer count);

    List<Review> getNReviewsForEachFilm(Integer count);

    void setLike(Integer reviewId, Integer userId);

    void setDislike(Integer reviewId, Integer userId);

    void removeLike(Integer reviewId, Integer userId);

    void removeDislike(Integer reviewId, Integer userId);
}
