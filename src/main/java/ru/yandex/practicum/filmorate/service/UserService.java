package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Objects;

@Service
public class UserService {
    private final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> findAll() {
        return storage.findAll();
    }

    public User create(User user) {
        validate(user);
        return storage.create(user);
    }

    public User update(User newUser) {
        validate(newUser);
        return storage.update(newUser);
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
        return storage.findAll()
                .stream()
                .anyMatch(user1 -> Objects.equals(email, user1.getEmail()));
    }
}
