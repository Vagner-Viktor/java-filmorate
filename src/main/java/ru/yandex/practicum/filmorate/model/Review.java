package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.constant.Usability;
import ru.yandex.practicum.filmorate.model.constant.Feedback;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    public Long reviewId;

    @NotBlank
    public Long filmId;

    @NotBlank
    public Long userId;

    @NotBlank
    public String content;

    @NotBlank
    public Feedback feedback;

    public Integer evaluation;

}
