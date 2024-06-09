package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;

    public Review createReview(Review review) {
        long id = reviewStorage.createReview(review);
        review.setReviewId(id);
        return review;
    }

    public Review updateReview(Review review) {
        reviewStorage.updateReview(review);
        return review;
    }

    public Review deleteReview(Integer id) {
        Review review = getReview(id);
        boolean haveBeedDeleted = reviewStorage.deleteReview(id);
        if (!haveBeedDeleted && review != null) return review;
        return null;
    }

    public Review getReview(Integer id) {
        Optional<Review> reviewOptional = reviewStorage.getReview(id);
        return reviewOptional.orElse(null);
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        if (count == null || count <= 0) count = 10;
        if (filmId != null) {
            return reviewStorage.getReviewsForFilm(filmId, count);
        }
        return reviewStorage.getNReviewsForEachFilm(count);
    }

    public Review likeReview(Integer reviewId, Integer userId) {
        reviewStorage.setLike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review dislikeReview(Integer reviewId, Integer userId) {
        reviewStorage.setDislike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review deleteLike(Integer reviewId, Integer userId) {
        reviewStorage.removeLike(reviewId, userId);
        return getReview(reviewId);
    }

    public Review deleteDislike(Integer reviewId, Integer userId) {
        reviewStorage.removeDislike(reviewId, userId);
        return getReview(reviewId);
    }
}
