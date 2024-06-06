package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.Collection;

public interface UserFeedStorage {
    UserFeed create(UserFeed userFeed);

    Collection<UserFeed> findUserFeeds(Long id);
}
