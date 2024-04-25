package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public Collection<User> findAll() {
        return service.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return service.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return service.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addToFriends(@PathVariable Long id,
                             @PathVariable Long friendId) {
        return service.addToFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFromFriends(@PathVariable Long id,
                                  @PathVariable Long friendId) {
        return service.deleteFromFriends(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> findAllFriends(@PathVariable Long id) {
        return service.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> findCommonFriends(@PathVariable Long id,
                                              @PathVariable Long otherId) {
        return service.findCommonFriends(id, otherId);
    }
}
