package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    User addToFriends(Long id, Long friendId);

    User deleteFromFriends(Long id, Long friendId);

    Collection<User> findAllFriends(Long id);

    Collection<User> findCommonFriends(Long id, Long otherId);

    Map<Long, User> getUsers();
}
