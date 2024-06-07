package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Director getDirectorById(Long id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    Long deleteDirector(Long id);
}
