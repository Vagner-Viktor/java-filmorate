package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;
import ru.yandex.practicum.filmorate.storage.UserFeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final UserFeedStorage userFeedStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public void delete(Long id) {
        userStorage.delete(id);
    }

    public User addToFriends(Long id, Long friendId) {
        if (!userStorage.isUserExists(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        if (!userStorage.isUserExists(friendId))
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        if (Objects.equals(id, friendId))
            throw new ValidationException("Нельзя добавить самого себя в друзья (id = " + id + ")");
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(id)
                .entityId(friendId)
                .timestamp(Instant.now())
                .eventType(EventType.FRIEND.name())
                .operation(OperationType.ADD.name())
                .build());
        return userStorage.addToFriends(id, friendId);
    }

    public User deleteFromFriends(Long id, Long friendId) {
        if (!userStorage.isUserExists(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        if (!userStorage.isUserExists(friendId))
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(id)
                .entityId(friendId)
                .timestamp(Instant.now())
                .eventType(EventType.FRIEND.name())
                .operation(OperationType.REMOVE.name())
                .build());
        return userStorage.deleteFromFriends(id, friendId);
    }

    public Collection<User> findAllFriends(Long id) {
        return userStorage.findAllFriends(id);
    }

    public Collection<User> findCommonFriends(Long id, Long otherId) {
        return userStorage.findCommonFriends(id, otherId);
    }

    public Collection<UserFeed> findUserFeeds(Long id) {
        if (!userStorage.isUserExists(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        return userFeedStorage.findUserFeeds(id);
    }
}
