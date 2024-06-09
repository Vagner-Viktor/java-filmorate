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

    //@Positive(message = "Film id cannot be negative.")
    @NotNull(message = "Film id cannot be null.")
    public Long filmId;

    //@Positive(message = "User id cannot be negative.")
    @NotNull(message = "User id cannot be blank.")
    public Long userId;

    @NotBlank(message = "Content cannot be blank.")
    public String content;

    @NotNull(message = "Positive cannot be blank.")
    public Boolean isPositive;

    public Integer useful;

}
