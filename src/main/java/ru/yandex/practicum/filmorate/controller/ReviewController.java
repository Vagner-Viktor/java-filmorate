package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // POST /reviews Добавление нового отзыва.
    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    // PUT /reviews Редактирование уже имеющегося отзыва.
    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    // DELETE /reviews/{id} Удаление уже имеющегося отзыва.
    @DeleteMapping("/{id}")
    public Review deleteReview(@PathVariable Integer id) {
        return reviewService.deleteReview(id);
    }

    // GET /reviews/{id} Получение отзыва по идентификатору.
    @GetMapping("/{id}")
    public Review getReview(@PathVariable Integer id) {
        return reviewService.getReview(id);
    }

    //GET /reviews?filmId={filmId}&count={count} Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано то 10.
    public List<Review> getReviews(@RequestParam(required = false) Long filmId,
                                   @RequestParam(required = false) Integer count) {
        return reviewService.getReviews(filmId, count);
    }

    // PUT /reviews/{id}/like/{userId} — пользователь ставит лайк отзыву.
    @PutMapping("/{id}/like/{userId}")
    public Review likeReview(@PathVariable Integer id,
                             @PathVariable Integer userId) {
        return reviewService.likeReview(id, userId);
    }

    // PUT /reviews/{id}/dislike/{userId} — пользователь ставит дизлайк отзыву.
    @PutMapping("/{id}/dislike/{userId}")
    public Review dislikeReview(@PathVariable Integer id,
                                @PathVariable Integer userId) {
        return reviewService.dislikeReview(id, userId);
    }

    // DELETE /reviews/{id}/like/{userId} — пользователь удаляет лайк отзыву.
    @DeleteMapping("/{id}/like/{userId}")
    public Review deleteLike(@PathVariable Integer id,
                             @PathVariable Integer userId) {
        return reviewService.deleteLike(id, userId);
    }

    // DELETE /reviews/{id}/dislike/{userId} — пользователь удаляет дизлайк отзыву.
    public Review deleteDislike(@PathVariable Integer id,
                             @PathVariable Integer userId) {
        return reviewService.deleteDislike(id, userId);
    }

}
