package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friend;

import java.util.Collection;

public interface FriendStorage {
    Collection<Friend> findFriendsOfUser(Long id);
}
