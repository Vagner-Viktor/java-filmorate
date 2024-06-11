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

    public Long reviewId;

    @NotNull(message = "Id фильма не может быть пустым.")
    public Long filmId;

    @NotNull(message = "Id пользователя не может быть пустым.")
    public Long userId;

    @NotBlank(message = "Контент не может быть пустым.")
    public String content;

    @NotNull(message = "Отзыв не может быть пустым.")
    public Boolean isPositive;

    public Integer useful;

}
