package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friend;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ContextConfiguration(classes = {FilmDbStorage.class,
        GenreDbStorage.class,
        UserDbStorage.class,
        MpaDbStorage.class,
        FilmLikeDbStorage.class,
        FriendDbStorage.class,
        FilmGenreDBStorage.class,
        FilmDirectorDBStorage.class})
@ComponentScan(basePackages = {"ru.yandex.practicum.filmorate.storage.mapper"})
class UserDbStorageTest {
    private final UserDbStorage userDbStorage;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @AllArgsConstructor
    static class ExpectedViolation {
        String propertyPath;
        String message;
    }

    public User getTestUser(int id) {
        switch (id) {
            case 1:
                User user1 = User.builder()
                        .id(null)
                        .name("User 1")
                        .email("user@ya.ru")
                        .login("userLogin1")
                        .birthday(LocalDate.of(2000, 2, 20))
                        .build();
                return user1;
            case 2:
                User user2 = User.builder()
                        .id(null)
                        .name("User 2")
                        .email("user2@ya.ru")
                        .login("userLogin2")
                        .birthday(LocalDate.of(2000, 2, 20))
                        .build();
                return user2;
            case 3:
                User user3 = User.builder()
                        .id(null)
                        .name("User 3")
                        .email("user3@ya.ru")
                        .login("userLogin3")
                        .birthday(LocalDate.of(2000, 2, 20))
                        .build();
                return user3;
            default:
                return null;
        }
    }

    @Test
    void findAll() {
        User user = getTestUser(1);
        userDbStorage.create(user);
        User user2 = getTestUser(2);
        userDbStorage.create(user2);

        Collection<User> responseEntity = userDbStorage.findAll();
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
    }

    @Test
    void create() {
        User user = getTestUser(1);
        userDbStorage.create(user);
        Collection<User> responseEntity = userDbStorage.findAll();
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertNotNull(responseEntity.iterator().next().getId());
        assertEquals(user.getLogin(), responseEntity.iterator().next().getLogin());
        assertEquals(user.getEmail(), responseEntity.iterator().next().getEmail());
        assertEquals(user.getName(), responseEntity.iterator().next().getName());
        assertEquals(user.getBirthday(), responseEntity.iterator().next().getBirthday());
    }

    @Test
    void update() {
        User user = getTestUser(1);
        Long userId = userDbStorage.create(user).getId();
        Collection<User> responseEntity = userDbStorage.findAll();
        User newUser = getTestUser(2);
        newUser.setId(userId);
        userDbStorage.update(newUser);
        responseEntity = userDbStorage.findAll();
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(newUser.getId(), responseEntity.iterator().next().getId());
        assertEquals(newUser.getLogin(), responseEntity.iterator().next().getLogin());
        assertEquals(newUser.getEmail(), responseEntity.iterator().next().getEmail());
        assertEquals(newUser.getName(), responseEntity.iterator().next().getName());
        assertEquals(newUser.getBirthday(), responseEntity.iterator().next().getBirthday());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "mail",
            "@ya.ru",
            "ya.ru",
            "m   ail@ya.ru",
            ".mail@ya.ru",
            "mail@ya.ru.",
            "m@il@ya.ru"
    })
    void createUserWithNotCorrectEMail(String email) {
        User user = getTestUser(1);
        user.setEmail(email);

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        ExpectedViolation expectedViolation = new ExpectedViolation(
                "email", "must be a well-formed email address");
        assertEquals(1, violations.size());
        assertEquals(
                expectedViolation.propertyPath,
                violations.get(0).getPropertyPath().toString()
        );
        assertEquals(
                expectedViolation.message,
                violations.get(0).getMessage()
        );
    }

    @Test
    void createUserWithNullEMail() {
        User user = getTestUser(1);
        user.setEmail(null);

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        ExpectedViolation expectedViolation = new ExpectedViolation(
                "email", "must not be blank");
        assertEquals(1, violations.size());
        assertEquals(
                expectedViolation.propertyPath,
                violations.get(0).getPropertyPath().toString()
        );
        assertEquals(
                expectedViolation.message,
                violations.get(0).getMessage()
        );
    }

    @Test
    void createUserWithBlankEMail() {
        User user = getTestUser(1);
        user.setEmail("");

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        ExpectedViolation expectedViolation = new ExpectedViolation(
                "email", "must not be blank");
        assertEquals(1, violations.size());
        assertEquals(
                expectedViolation.propertyPath,
                violations.get(0).getPropertyPath().toString()
        );
        assertEquals(
                expectedViolation.message,
                violations.get(0).getMessage()
        );
    }

    @Test
    void createUserWithNullLogin() {
        User user = getTestUser(1);
        user.setLogin(null);

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        ExpectedViolation expectedViolation = new ExpectedViolation(
                "login", "must not be blank");
        assertEquals(1, violations.size());
        assertEquals(
                expectedViolation.propertyPath,
                violations.get(0).getPropertyPath().toString()
        );
        assertEquals(
                expectedViolation.message,
                violations.get(0).getMessage()
        );
    }

    @Test
    void createUserWithBlankLogin() {
        User user = getTestUser(1);
        user.setLogin("");

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        ExpectedViolation expectedViolation = new ExpectedViolation(
                "login", "must not be blank");
        assertEquals(1, violations.size());
        assertEquals(
                expectedViolation.propertyPath,
                violations.get(0).getPropertyPath().toString()
        );
        assertEquals(
                expectedViolation.message,
                violations.get(0).getMessage()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "log  in",
            "  login",
            "login "
    })
    void createUserWithSpaceInLogin(String login) {
        User user = getTestUser(1);
        user.setLogin(login);

        Exception exception = assertThrows(
                ValidationException.class,
                () -> userDbStorage.create(user)
        );
        assertEquals(
                "Логин не может содержать пробелов",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Create a user without a name then name=login")
    void createUserWithoutName() {
        User user = getTestUser(1);
        user.setName(null);
        userDbStorage.create(user);
        Collection<User> responseEntity = userDbStorage.findAll();
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertNotNull(responseEntity.iterator().next().getId());
        assertEquals(user.getLogin(), responseEntity.iterator().next().getLogin());
        assertEquals(user.getEmail(), responseEntity.iterator().next().getEmail());
        assertEquals(user.getLogin(), responseEntity.iterator().next().getName());
        assertEquals(user.getBirthday(), responseEntity.iterator().next().getBirthday());
    }

    @Test
    void createUserWithBirthdayAfterNow() {
        User user = getTestUser(1);
        user.setBirthday(LocalDate.now().plusDays(1));

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));
        System.out.println(violations);
        ExpectedViolation expectedViolation = new ExpectedViolation(
                "birthday", "must be a date in the past or in the present");
        assertEquals(1, violations.size());
        assertEquals(
                expectedViolation.propertyPath,
                violations.get(0).getPropertyPath().toString()
        );
        assertEquals(
                expectedViolation.message,
                violations.get(0).getMessage()
        );
    }

    @Test
    void createUserWithBirthdayNow() {
        User user = getTestUser(1);
        user.setBirthday(LocalDate.now());

        userDbStorage.create(user);
        Collection<User> responseEntity = userDbStorage.findAll();
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertNotNull(responseEntity.iterator().next().getId());
        assertEquals(user.getLogin(), responseEntity.iterator().next().getLogin());
        assertEquals(user.getEmail(), responseEntity.iterator().next().getEmail());
        assertEquals(user.getName(), responseEntity.iterator().next().getName());
        assertEquals(user.getBirthday(), responseEntity.iterator().next().getBirthday());
    }

    @Test
    void addFriend() {
        User user = getTestUser(1);
        Long user1Id = userDbStorage.create(user).getId();
        User user2 = getTestUser(2);
        Long user2Id = userDbStorage.create(user2).getId();
        userDbStorage.addToFriends(user1Id, user2Id);
        List<User> responseEntity = new ArrayList<>(userDbStorage.findAll());
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
        assertTrue(responseEntity.get(0).getFriends().contains(new Friend(user2Id, 2)));
    }

    @Test
    void deleteFriend() {
        User user = getTestUser(1);
        Long user1Id = userDbStorage.create(user).getId();
        User user2 = getTestUser(2);
        Long user2Id = userDbStorage.create(user2).getId();
        userDbStorage.addToFriends(user1Id, user2Id);
        userDbStorage.deleteFromFriends(user1Id, user2Id);
        List<User> responseEntity = new ArrayList<>(userDbStorage.findAll());
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
        assertFalse(responseEntity.get(0).getFriends().contains(new Friend(user2Id, 2)));
    }

    @Test
    void findAllFriends() {
        User user = getTestUser(1);
        Long user1Id = userDbStorage.create(user).getId();
        User user2 = getTestUser(2);
        Long user2Id = userDbStorage.create(user2).getId();
        User user3 = getTestUser(3);
        Long user3Id = userDbStorage.create(user3).getId();
        userDbStorage.addToFriends(user1Id, user2Id);
        userDbStorage.addToFriends(user1Id, user3Id);
        List<User> responseEntity = new ArrayList<>(userDbStorage.findAllFriends(user1Id));
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
        assertEquals(responseEntity.get(0).getId(), user2Id);
        assertEquals(responseEntity.get(1).getId(), user3Id);
    }

    @Test
    void findCommonFriends() {
        User user = getTestUser(1);
        Long user1Id = userDbStorage.create(user).getId();
        User user2 = getTestUser(2);
        Long user2Id = userDbStorage.create(user2).getId();
        User user3 = getTestUser(3);
        Long user3Id = userDbStorage.create(user3).getId();
        userDbStorage.addToFriends(user1Id, user2Id);
        userDbStorage.addToFriends(user1Id, user3Id);
        List<User> responseEntity = new ArrayList<>(userDbStorage.findCommonFriends(user2Id, user3Id));
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(responseEntity.get(0).getId(), user1Id);
    }
}