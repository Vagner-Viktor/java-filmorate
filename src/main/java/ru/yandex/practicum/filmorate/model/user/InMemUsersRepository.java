package ru.yandex.practicum.filmorate.model.user;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class InMemUsersRepository implements UsersRepository {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Получение списка пользователей");
        return users.values();
    }

    @Override
    public User create(User user) {
        log.info("Добавление нового пользователя");
        validate(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} добавлен в список с id = {}", user.getName(), user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id пользователя должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            validate(newUser);
            users.put(newUser.getId(), newUser);
            log.error("Пользователь с id = {} обновлен", newUser.getId());
            return newUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
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
