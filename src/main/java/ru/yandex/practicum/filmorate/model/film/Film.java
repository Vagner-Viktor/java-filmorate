package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Duration;
import java.time.LocalDate;


@Data
@Builder
public class Film {
    private Long id;
    private static final int DESCRIPTION_MAX_SIZE = 200;

    @NotBlank
    private String name;

    @Size(max = DESCRIPTION_MAX_SIZE)
    private String description;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotNull
    private Duration duration;

    @JsonProperty("duration")
    @Positive
    public long getDurationTimeSeconds() {
        return duration.getSeconds();
    }
}
