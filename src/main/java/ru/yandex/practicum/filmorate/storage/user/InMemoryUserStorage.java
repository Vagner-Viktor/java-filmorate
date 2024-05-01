package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Получение списка пользователей");
        return users.values();
    }

    @Override
    public boolean checkUserExists(Long id) {
        return users.containsKey(id);
    }

    @Override
    public User create(User user) {
        validate(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} добавлен в список с id = {}", user.getName(), user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new NotFoundException("Id пользователя должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            validate(newUser);
            users.put(newUser.getId(), newUser);
            log.info("Пользователь с id = {} обновлен", newUser.getId());
            return newUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public User addToFriends(Long id, Long friendId) {
        if (!users.containsKey(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        if (!users.containsKey(friendId))
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        if (id == friendId)
            throw new ValidationException("Нельзя добавить самого себя в друзья (id = " + id + ")");
        users.get(id).addFriend(friendId);
        users.get(friendId).addFriend(id);
        log.info("Пользователь с id = {} и пользователь с id = {} теперь друзья", friendId, id);
        return users.get(id);
    }

    @Override
    public User deleteFromFriends(Long id, Long friendId) {
        if (!users.containsKey(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        if (!users.containsKey(friendId))
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        users.get(id).deleteFriend(friendId);
        users.get(friendId).deleteFriend(id);
        log.info("Пользователь с id = {} и пользователь с id = {} больше не друзья", friendId, id);
        return users.get(id);
    }

    @Override
    public Collection<User> findAllFriends(Long id) {
        if (!users.containsKey(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        log.info("Поиск друзей пользователя с id = {}", id);
        return users.values().stream()
                .filter(user -> users.get(id).getFriends().containsKey(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> findCommonFriends(Long id, Long otherId) {
        if (!users.containsKey(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        if (!users.containsKey(otherId))
            throw new NotFoundException("Пользователь с id = " + otherId + " не найден");
        log.info("Поиск общих друзей пользователя с id = {} и пользователя с id = {}", id, otherId);
        Set<Long> commonFriendId = users.get(id).getFriends().keySet().stream()
                .filter(friendId -> users.get(otherId).getFriends().containsKey(friendId))
                .collect(Collectors.toSet());
        return users.values().stream()
                .filter(user -> commonFriendId.contains(user.getId()))
                .collect(Collectors.toList());
    }

    private void validate(User user) {
        if (checkDuplicatedEmail(user.getEmail())) {
            throw new DuplicatedDataException("Этот e-mail уже используется");
        }
        if (user.getLogin().indexOf(" ") != -1) {
            throw new ValidationException("Логин не может содержать пробелов");
        }
        if (user.getName() == null || user.getName().isBlank()) user.setName(user.getLogin());
    }

    private boolean checkDuplicatedEmail(String email) {
        return users.values()
                .stream()
                .anyMatch(user1 -> Objects.equals(email, user1.getEmail()));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}