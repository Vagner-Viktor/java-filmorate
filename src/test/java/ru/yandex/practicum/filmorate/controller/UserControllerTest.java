package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserInMemoryStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    UserStorage storage = new UserInMemoryStorage();
    UserService service = new UserService(storage);
    UserController controller = new UserController(service);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @AllArgsConstructor
    static class ExpectedViolation {
        String propertyPath;
        String message;
    }

    @Test
    void findAll() {
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        controller.create(user);
        User user2 = User.builder()
                .id(null)
                .name("User 2")
                .email("user2@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2002, 4, 20))
                .build();
        controller.create(user2);

        Collection<User> responseEntity = controller.findAll();
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
    }

    @Test
    void create() {
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        controller.create(user);
        Collection<User> responseEntity = controller.findAll();
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
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        controller.create(user);
        Collection<User> responseEntity = controller.findAll();
        User newUser = User.builder()
                .id(responseEntity.iterator().next().getId())
                .name("User 2")
                .email("user2@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2002, 2, 20))
                .build();
        controller.update(newUser);
        responseEntity = controller.findAll();
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
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email(email)
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        FilmControllerTest.ExpectedViolation expectedViolation = new FilmControllerTest.ExpectedViolation(
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
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email(null)
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        FilmControllerTest.ExpectedViolation expectedViolation = new FilmControllerTest.ExpectedViolation(
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
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        FilmControllerTest.ExpectedViolation expectedViolation = new FilmControllerTest.ExpectedViolation(
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
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("mail@ya.ru")
                .login(null)
                .birthday(LocalDate.of(2000, 2, 20))
                .build();

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        FilmControllerTest.ExpectedViolation expectedViolation = new FilmControllerTest.ExpectedViolation(
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
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("mail@ya.ru")
                .login("")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));

        FilmControllerTest.ExpectedViolation expectedViolation = new FilmControllerTest.ExpectedViolation(
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
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("mail@ya.ru")
                .login(login)
                .birthday(LocalDate.of(2000, 2, 20))
                .build();

        Exception exception = assertThrows(
                ValidationException.class,
                () -> controller.create(user)
        );
        assertEquals(
                "Логин не может содержать пробелов",
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Create a user without a name then name=login")
    void createUserWithoutName() {
        User user = User.builder()
                .id(null)
                .name(null)
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        controller.create(user);
        Collection<User> responseEntity = controller.findAll();
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
        User user = User.builder()
                .id(null)
                .name(null)
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        List<ConstraintViolation<User>> violations = new ArrayList<>(validator.validate(user));
        System.out.println(violations);
        FilmControllerTest.ExpectedViolation expectedViolation = new FilmControllerTest.ExpectedViolation(
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
        User user = User.builder()
                .id(null)
                .name(null)
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.now())
                .build();

        controller.create(user);
        Collection<User> responseEntity = controller.findAll();
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
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user1Id = controller.create(user).getId();
        User user2 = User.builder()
                .id(null)
                .name("User 2")
                .email("user2@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2002, 2, 20))
                .build();
        Long user2Id = controller.create(user2).getId();
        controller.addToFriends(user1Id, user2Id);
        List<User> responseEntity = new ArrayList<>(controller.findAll());
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
        assertTrue(responseEntity.get(0).getFriends().containsKey(user2Id));
        assertTrue(responseEntity.get(1).getFriends().containsKey(user1Id));
    }

    @Test
    void deleteFriend() {
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user1Id = controller.create(user).getId();
        User user2 = User.builder()
                .id(null)
                .name("User 2")
                .email("user2@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2002, 2, 20))
                .build();
        Long user2Id = controller.create(user2).getId();
        controller.addToFriends(user1Id, user2Id);
        controller.deleteFromFriends(user1Id, user2Id);
        List<User> responseEntity = new ArrayList<>(controller.findAll());
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
        assertFalse(responseEntity.get(0).getFriends().containsKey(user2Id));
        assertFalse(responseEntity.get(1).getFriends().containsKey(user1Id));
    }

    @Test
    void findAllFriends() {
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user1Id = controller.create(user).getId();
        User user2 = User.builder()
                .id(null)
                .name("User 2")
                .email("user2@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2002, 2, 20))
                .build();
        Long user2Id = controller.create(user2).getId();
        User user3 = User.builder()
                .id(null)
                .name("User 3")
                .email("user3@ya.ru")
                .login("userLogin3")
                .birthday(LocalDate.of(2002, 2, 20))
                .build();
        Long user3Id = controller.create(user3).getId();
        controller.addToFriends(user1Id, user2Id);
        controller.addToFriends(user1Id, user3Id);
        List<User> responseEntity = new ArrayList<>(controller.findAllFriends(user1Id));
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
        assertEquals(responseEntity.get(0).getId(), user2Id);
        assertEquals(responseEntity.get(1).getId(), user3Id);
    }

    @Test
    void findCommonFriends() {
        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user1Id = controller.create(user).getId();
        User user2 = User.builder()
                .id(null)
                .name("User 2")
                .email("user2@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2002, 2, 20))
                .build();
        Long user2Id = controller.create(user2).getId();
        User user3 = User.builder()
                .id(null)
                .name("User 3")
                .email("user3@ya.ru")
                .login("userLogin3")
                .birthday(LocalDate.of(2002, 2, 20))
                .build();
        Long user3Id = controller.create(user3).getId();
        controller.addToFriends(user1Id, user2Id);
        controller.addToFriends(user1Id, user3Id);
        List<User> responseEntity = new ArrayList<>(controller.findCommonFriends(user2Id, user3Id));
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(responseEntity.get(0).getId(), user1Id);
    }
}