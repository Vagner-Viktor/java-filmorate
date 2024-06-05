package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchType;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(Long id);

    Film create(Film film);

    Film update(Film newFilm);

    Film addLike(Long id, Long userId);

    Film deleteLike(Long id, Long userId);

    Collection<Film> getPopular(Long count);

    Collection<Film> searchFilms(String query, SearchType searchType);
}
