package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[\\w!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&amp;'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validateEmail(user.getEmail());
        validateLogin(user.getLogin());
        if (!validateName(user.getName())) user.setName(user.getLogin());
        validateBirthday(user.getBirthday());
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) throw new ConditionsNotMetException("Id должен быть указан");
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null &&
                    !newUser.getEmail().isBlank() &&
                    validateEmail(newUser.getEmail())) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null &&
                    !newUser.getLogin().isBlank() &&
                    validateLogin(newUser.getLogin())) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null &&
                    !newUser.getName().isBlank() &&
                    validateName(newUser.getName())) {
                oldUser.setName(newUser.getName());
            }
            if (newUser.getBirthday() != null && validateBirthday(newUser.getBirthday()))
                oldUser.setBirthday(newUser.getBirthday());
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private boolean validateEmail(String email) {
        if (email == null || email.isBlank())
            throw new ConditionsNotMetException("Имейл должен быть указан");
        if (checkDuplicatedEmail(email)) throw new ConditionsNotMetException("Этот имейл уже используется");
        if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches())
            throw new ValidationException("Указан не корректный имейл");
        return true;
    }

    private boolean checkDuplicatedEmail(String email) {
        return users.values()
                .stream()
                .anyMatch(user1 -> Objects.equals(email, user1.getEmail()));
    }

    private boolean validateLogin(String login) {
        if (login == null || login.isBlank())
            throw new ConditionsNotMetException("Логин должен быть указан");
        if (login.contains(" ")) throw new ValidationException("Логин не может содержать пробелов");
        return true;
    }

    private boolean validateName(String name) {
        if (name == null || name.isBlank())
            return false;
        return true;
    }

    private boolean validateBirthday(Instant birthday) {
        if (birthday != null && birthday.isAfter(Instant.now()))
            throw new ValidationException("Дата рождения не может быть больше текущей");
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
