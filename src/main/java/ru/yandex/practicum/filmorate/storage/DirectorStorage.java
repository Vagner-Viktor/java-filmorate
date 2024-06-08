package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;

public interface DirectorStorage {
    Collection<Director> getAllDirectors();

    Director getDirectorById(Long id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    Long deleteDirector(Long id);
}
