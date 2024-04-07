package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получение списка фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Добавление нового фильма");
        validateName(film.getName());
        validateDescription(film.getDescription());
        validateReleaseDate(film.getReleaseDate());
        validateDuration(film.getDuration());
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} добавлен в список с id = {}", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Id фильма должен быть указан");
            throw new ConditionsNotMetException("Id фильма должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.info("Обновление данных фильма {} с id = {}", oldFilm.getName(), oldFilm.getId());
            if (newFilm.getName() != null &&
                    !newFilm.getName().isBlank() &&
                    validateName(newFilm.getName())) {
                log.info("Изменено название фильма с {} на {}", oldFilm.getName(), newFilm.getName());
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getDescription() != null &&
                    !newFilm.getDescription().isBlank() &&
                    validateDescription(newFilm.getDescription())) {
                log.info("Изменено описание фильма с {} на {}", oldFilm.getDescription(), newFilm.getDescription());
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getReleaseDate() != null &&
                    validateReleaseDate(newFilm.getReleaseDate())) {
                log.info("Изменена дата релиза фильма с {} на {}", oldFilm.getReleaseDate(), newFilm.getReleaseDate());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getDuration() != null &&
                    validateDuration(newFilm.getDuration())) {
                log.info("Изменена продолжительность фильма с {} на {}", oldFilm.getDuration(), newFilm.getDuration());
                oldFilm.setDuration(newFilm.getDuration());
            }
            log.info("Фильм с id = {} обновлен", oldFilm.getId());
            return oldFilm;
        }
        log.error("Фильм с id = {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private boolean validateName(String name) {
        if (name == null || name.isBlank()) {
            log.error("Название фильма должно быть указано!");
            throw new ConditionsNotMetException("Название фильма должно быть указано!");
        }
        return true;
    }

    private boolean validateDescription(String description) {
        if (description != null && !description.isBlank()) {
            if (description.length() > 200) {
                log.error("Размер описание не может быть больше 200 символов!");
                throw new ValidationException("Размер описание не может быть больше 200 символов!");
            }
        }
        return true;
    }

    private boolean validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            log.error("Дата релиза фильма должна быть указана!");
            throw new ConditionsNotMetException("Дата релиза фильма должна быть указана!");
        }
        if (releaseDate.isBefore(LocalDate.of(1895, 1, 28))) {
            log.error("Дата релиза ({}) не может быть раньше 28 декабря 1895 года!", releaseDate);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
        return true;
    }

    private boolean validateDuration(Duration duration) {
        if (duration == null || duration.toSeconds() <= 0) {
            log.error("Продолжительность фильма должна быть положительным числом!");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом!");
        }
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
