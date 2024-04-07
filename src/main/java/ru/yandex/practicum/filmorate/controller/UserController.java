package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[\\w!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&amp;'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получение списка пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Добавление нового пользователя");
        validateEmail(user.getEmail());
        validateLogin(user.getLogin());
        if (!validateName(user.getName())) user.setName(user.getLogin());
        validateBirthday(user.getBirthday());
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} добавлен в список с id = {}", user.getName(), user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.error("Id пользователя должен быть указан");
            throw new ConditionsNotMetException("Id пользователя должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            log.info("Обновление данных пользователя {} с id = {}", oldUser.getName(), oldUser.getId());
            if (newUser.getEmail() != null &&
                    !newUser.getEmail().isBlank() &&
                    validateEmail(newUser.getEmail())) {
                log.info("Изменен e-mail с {} на {}", oldUser.getEmail(), newUser.getEmail());
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null &&
                    !newUser.getLogin().isBlank() &&
                    validateLogin(newUser.getLogin())) {
                log.info("Изменен логин с {} на {}", oldUser.getLogin(), newUser.getLogin());
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null &&
                    !newUser.getName().isBlank() &&
                    validateName(newUser.getName())) {
                log.info("Изменено имя с {} на {}", oldUser.getName(), newUser.getName());
                oldUser.setName(newUser.getName());
            }
            if (newUser.getBirthday() != null && validateBirthday(newUser.getBirthday())) {
                log.info("Изменена дата рождения с {} на {}", oldUser.getBirthday(), newUser.getBirthday());
                oldUser.setBirthday(newUser.getBirthday());
            }
            log.error("пользователь с id = {} обновлен", oldUser.getId());
            return oldUser;
        }
        log.error("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private boolean validateEmail(String email) {
        if (email == null || email.isBlank()) {
            log.error("E-mail должен быть указан");
            throw new ConditionsNotMetException("E-mail должен быть указан");
        }
        if (checkDuplicatedEmail(email)) {
            log.error("Этот e-mail уже используется");
            throw new ConditionsNotMetException("Этот e-mail уже используется");
        }
        if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()) {
            log.error("{} - не корректный e-mail", email);
            throw new ValidationException(email + " не корректный e-mail");
        }
        return true;
    }

    private boolean checkDuplicatedEmail(String email) {
        return users.values()
                .stream()
                .anyMatch(user1 -> Objects.equals(email, user1.getEmail()));
    }

    private boolean validateLogin(String login) {
        if (login == null || login.isBlank()) {
            log.error("Логин должен быть указан");
            throw new ConditionsNotMetException("Логин должен быть указан");
        }
        if (login.indexOf(" ") != -1) {
            log.error("Логин не может содержать пробелов");
            throw new ValidationException("Логин не может содержать пробелов");
        }
        return true;
    }

    private boolean validateName(String name) {
        if (name == null || name.isBlank())
            return false;
        return true;
    }

    private boolean validateBirthday(LocalDate birthday) {
        LocalDate now = LocalDate.now();
        if (birthday != null && birthday.isAfter(now)) {
            log.error("Дата рождения {} не может быть больше текущей {}", birthday, now);
            throw new ValidationException("Дата рождения " + birthday + " не может быть больше текущей " + now);
        }
        return true;
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
