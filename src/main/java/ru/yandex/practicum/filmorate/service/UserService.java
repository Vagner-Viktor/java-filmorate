package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
public class UserService {

    @Autowired
    private final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> findAll() {
        return storage.findAll();
    }

    public User create(User user) {
        return storage.create(user);
    }

    public User update(User newUser) {
        return storage.update(newUser);
    }

    public User addToFriends(Long id, Long friendId) {
        return storage.addToFriends(id, friendId);
    }

    public User deleteFromFriends(Long id, Long friendId) {
        return storage.deleteFromFriends(id, friendId);
    }

    public Collection<User> findAllFriends(Long id) {
        return storage.findAllFriends(id);
    }

    public Collection<User> findCommonFriends(Long id, Long otherId) {
        return storage.findCommonFriends(id, otherId);
    }
}
