package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface MpaStorage {
    Collection<Mpa> findAll();

    Mpa findById(int id);

    boolean isMpaExists(int id);
}
