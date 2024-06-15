package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private Long reviewId;

    @NotNull(message = "Id фильма не может быть пустым.")
    private Long filmId;

    @NotNull(message = "Id пользователя не может быть пустым.")
    private Long userId;

    @NotBlank(message = "Контент не может быть пустым.")
    private String content;

    @NotNull(message = "Отзыв не может быть пустым.")
    private Boolean isPositive;

    private Integer useful;

}
