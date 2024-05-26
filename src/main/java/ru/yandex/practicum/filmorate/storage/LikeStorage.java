package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;

public interface LikeStorage {
    Collection<Long> findLikesOfFilm(Long id);
}
