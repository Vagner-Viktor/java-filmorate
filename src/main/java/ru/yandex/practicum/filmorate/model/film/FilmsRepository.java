package ru.yandex.practicum.filmorate.model.film;

import java.util.Collection;

public interface FilmsRepository {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film newFilm);
}
