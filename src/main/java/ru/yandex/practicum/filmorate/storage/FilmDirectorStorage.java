package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmDirector;

import java.util.Collection;

public interface FilmDirectorStorage {
    Collection<FilmDirector> findDirectorsOfFilms(String filmsId);
}
