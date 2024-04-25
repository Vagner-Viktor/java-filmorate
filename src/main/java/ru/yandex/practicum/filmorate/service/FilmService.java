package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class FilmService {
    private final FilmStorage storage;

    @Autowired
    public FilmService(FilmStorage storage) {
        this.storage = storage;
    }

    public Collection<Film> findAll() {
        return storage.findAll();
    }

    public Film create(Film film) {
        validate(film);
        return storage.create(film);
    }

    public Film update(Film newFilm) {
        validate(newFilm);
        return storage.update(newFilm);
    }

    public Film addLike(Long id, Long userId) {
        return storage.addLike(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        return storage.deleteLike(id, userId);
    }

    public Collection<Film> getPopular(Long count) {
        return storage.getPopular(count);
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
    }
}
