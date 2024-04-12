package ru.yandex.practicum.filmorate.model.user;

import java.util.Collection;

public interface UsersRepository {
    Collection<User> findAll();

    User create(User user);

    User update(User newUser);
}
