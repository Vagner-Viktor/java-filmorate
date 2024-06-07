package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage storage;

    public Collection<Film> findAll() {
        return storage.findAll();
    }

    public Film findById(Long id) {
        return storage.findById(id);
    }

    public Film create(Film film) {
        validate(film);
        return storage.create(film);
    }

    public Film update(Film newFilm) {
        validate(newFilm);
        return storage.update(newFilm);
    }

    public void delete(Long id) {
          storage.delete(id);
    }

    public Film addLike(Long id, Long userId) {
        return storage.addLike(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        return storage.deleteLike(id, userId);
    }

    public Collection<Film> getPopular(Long count, Long genreId, int year) {
        return storage.getPopular(count, genreId, year);
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
    }
}
