package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.user.InMemUsersRepository;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.model.user.UsersRepository;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UsersRepository usersRepository = new InMemUsersRepository();

    @GetMapping
    public Collection<User> findAll() {
        return usersRepository.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return usersRepository.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return usersRepository.update(newUser);
    }
}
