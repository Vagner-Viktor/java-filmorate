package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validateName(film.getName());
        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());
        film.setId(getNextId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null &&
                    !newFilm.getName().isBlank() &&
                    validateName(newFilm.getName()))
                oldFilm.setName(newFilm.getName());
            if (newFilm.getDescription() != null &&
                    newFilm.getDescription().isBlank() &&
                    validateDescription(newFilm.getDescription()))
                oldFilm.setDescription(newFilm.getDescription());
            if (newFilm.getReleaseDate() != null && validateReleaseDate(newFilm.getReleaseDate()))
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            if (newFilm.getDuration() != null &&
                    validateDuration(newFilm.getDuration()))
                oldFilm.setDuration(newFilm.getDuration());
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private boolean validateName(String name) {
        if (name == null || name.isBlank())
            throw new ConditionsNotMetException("Название фильма должно быть указано!");
        return true;
    }

    private boolean validateDescription(String description) {
        if (description != null && !description.isBlank()) {
            if (description.length() > 200)
                throw new ValidationException("Размер описание не может быть больше 200 символов!");
        }
        return true;
    }

    private boolean validateReleaseDate(Instant releaseDate) {
        if (releaseDate == null)
            throw new ConditionsNotMetException("Дата релиза фильма должна быть указана!");
        if (releaseDate.isBefore(Instant.parse("1895-12-28T00:00:00Z")))
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года;");
        return true;
    }

    private boolean validateDuration(Duration duration) {
        if (duration == null || duration.getNano() == 0)
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        return true;
    }

    private Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
