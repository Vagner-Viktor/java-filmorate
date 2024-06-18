package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmLike {

    @JsonBackReference
    @NotNull
    private Long filmId;

    @JsonProperty("user_id")
    @NotNull
    private Long userId;

    @Size(min = 0, max = 10, message = "Rating не передан или не корректный.")
    private Integer mark;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilmLike filmLike = (FilmLike) o;
        return filmId.equals(filmLike.filmId) && userId.equals(filmLike.userId);
    }

    @Override
    public int hashCode() {
        int result = filmId.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }
}
