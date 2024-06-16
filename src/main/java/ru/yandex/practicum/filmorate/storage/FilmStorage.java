package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchType;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(Long id);

    Film create(Film film);

    Film update(Film newFilm);

    void delete(Long id);

    Film addLike(Long id, Long userId, Float rating);

    Film deleteLike(Long id, Long userId);

    boolean isFilmExists(Long id);

    Collection<Film> searchFilms(String query, SearchType searchType);

    Collection<Film> getPopular(Long count, Long genreId, int year);

    Collection<Film> getFilmsByDirector(Long id, String sortBy);

    Collection<Film> getRecommendedFilmsForUser(Long id);

    Collection<Film> getCommonFilms(Long userId, Long friendId);
}
