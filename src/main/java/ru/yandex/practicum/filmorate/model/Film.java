package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    private RatingMPA rating;

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @JsonProperty("duration")
    @Positive
    public long getDurationTimeSeconds() {
        return duration.getSeconds();
    }

    public void addLike(Long id) {
        likes.add(id);
    }

    public void deleteLike(Long id) {
        likes.remove(id);
    }

    public int getLikesCount() {
        return likes.size();
    }
}

