package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private final UserStorage userStorage = new InMemoryUserStorage();
    private final FilmStorage storage = new InMemoryFilmStorage(userStorage);
    private final FilmService service = new FilmService(storage);
    private final FilmController controller = new FilmController(service);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @AllArgsConstructor
    static class ExpectedViolation {
        String propertyPath;
        String message;
    }

    @BeforeEach
    void setUp() {

    }


    @Test
    void findAll() {
        Film film = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        controller.create(film);
        Film film2 = Film.builder()
                .id(null)
                .name("Фильм №2")
                .description("Описание фильма №2")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        controller.create(film2);

        Collection<Film> responseEntity = controller.findAll();
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
    }

    @Test
    void create() {
        Film film = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        controller.create(film);
        Collection<Film> responseEntity = controller.findAll();
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertNotNull(responseEntity.iterator().next().getId());
        assertEquals(film.getName(), responseEntity.iterator().next().getName());
        assertEquals(film.getDescription(), responseEntity.iterator().next().getDescription());
        assertEquals(film.getReleaseDate(), responseEntity.iterator().next().getReleaseDate());
        assertEquals(film.getDuration(), responseEntity.iterator().next().getDuration());
    }

    @Test
    void update() {
        Film film = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        controller.create(film);
        Collection<Film> responseEntity = controller.findAll();
        final Film newFilm = Film.builder()
                .id(responseEntity.iterator().next().getId())
                .name("Фильм №2")
                .description("Описание фильма №2")
                .releaseDate(LocalDate.now().minusMonths(1))
                .duration(Duration.ofMinutes(50))
                .build();
        controller.update(newFilm);
        responseEntity = controller.findAll();

        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(newFilm.getName(), responseEntity.iterator().next().getName());
        assertEquals(newFilm.getDescription(), responseEntity.iterator().next().getDescription());
        assertEquals(newFilm.getReleaseDate(), responseEntity.iterator().next().getReleaseDate());
        assertEquals(newFilm.getDuration(), responseEntity.iterator().next().getDuration());
    }

    @Test
    void createNullNameFilm() {
        Film film = Film.builder()
                .id(null)
                .name(null)
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        List<ConstraintViolation<Film>> violations = new ArrayList<>(validator.validate(film));

        ExpectedViolation expectedViolation = new ExpectedViolation(
                "name", "must not be blank");
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
    void createDescriptionMore200() {
        Film film = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова, который задолжал им деньги, а именно 20 миллионов. о Куглов, который за время «своего отсутствия», стал кандидатом Коломбани.")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        List<ConstraintViolation<Film>> violations = new ArrayList<>(validator.validate(film));

        ExpectedViolation expectedViolation = new ExpectedViolation(
                "description", "size must be between 0 and 200");
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
    @ValueSource(ints = {
            27,
            28,
            29
    })
    void createFilmWinReleaseDateBefore_1895_01_28(int releaseDay) {
        Film film = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.of(1895, 1, releaseDay))
                .duration(Duration.ofMinutes(90))
                .build();

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            Exception exception = assertThrows(
                    ValidationException.class,
                    () -> controller.create(film)
            );
            assertEquals(
                    "Дата релиза не может быть раньше 28 декабря 1895 года!",
                    exception.getMessage()
            );
        } else {
            controller.create(film);
            Collection<Film> responseEntity = controller.findAll();
            assertNotNull(responseEntity);
            assertEquals(1, responseEntity.size());
            assertNotNull(responseEntity.iterator().next().getId());
            assertEquals(film.getName(), responseEntity.iterator().next().getName());
            assertEquals(film.getDescription(), responseEntity.iterator().next().getDescription());
            assertEquals(film.getReleaseDate(), responseEntity.iterator().next().getReleaseDate());
            assertEquals(film.getDuration(), responseEntity.iterator().next().getDuration());
        }
    }

    @Test
    void createFilmWinNegativeDuration() {
        Film film = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(-90))
                .build();
        List<ConstraintViolation<Film>> violations = new ArrayList<>(validator.validate(film));
        System.out.println(violations);
        ExpectedViolation expectedViolation = new ExpectedViolation(
                "durationTimeSeconds", "must be greater than 0");
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
    void addLike() {
        Film film = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        Long filmId = controller.create(film).getId();

        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user1Id = userStorage.create(user).getId();

        controller.addLike(filmId, user1Id);

        ArrayList<Film> responseEntity = new ArrayList<>(controller.findAll());
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(1, responseEntity.get(0).getLikesCount());
        assertTrue(responseEntity.get(0).getLikes().contains(user1Id));
    }

    @Test
    void deleteLike() {
        Film film = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        Long filmId = controller.create(film).getId();

        User user = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user1Id = userStorage.create(user).getId();

        controller.addLike(filmId, user1Id);
        controller.deleteLike(filmId, user1Id);

        ArrayList<Film> responseEntity = new ArrayList<>(controller.findAll());
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(0, responseEntity.get(0).getLikesCount());
    }

    @Test
    void getPopularCount10() {
        Film film1 = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        Long film1Id = controller.create(film1).getId();
        Film film2 = Film.builder()
                .id(null)
                .name("Фильм №2")
                .description("Описание фильма №2")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        Long film2Id = controller.create(film2).getId();
        Film film3 = Film.builder()
                .id(null)
                .name("Фильм №3")
                .description("Описание фильма №3")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        Long film3Id = controller.create(film3).getId();

        User user1 = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user1Id = userStorage.create(user1).getId();
        User user2 = User.builder()
                .id(null)
                .name("User 2")
                .email("user2@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user2Id = userStorage.create(user2).getId();
        User user3 = User.builder()
                .id(null)
                .name("User 3")
                .email("user3@ya.ru")
                .login("userLogin3")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user3Id = userStorage.create(user3).getId();

        controller.addLike(film2Id, user1Id);
        controller.addLike(film2Id, user2Id);
        controller.addLike(film2Id, user3Id);

        controller.addLike(film3Id, user1Id);
        controller.addLike(film3Id, user2Id);


        ArrayList<Film> responseEntity = new ArrayList<>(controller.getPopular(10L));
        assertNotNull(responseEntity);
        assertEquals(3, responseEntity.size());
        assertEquals(film2Id, responseEntity.get(0).getId());
        assertEquals(film3Id, responseEntity.get(1).getId());
        assertEquals(film1Id, responseEntity.get(2).getId());
    }

    @Test
    void getPopularCount1() {
        Film film1 = Film.builder()
                .id(null)
                .name("Фильм №1")
                .description("Описание фильма №1")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        Long film1Id = controller.create(film1).getId();
        Film film2 = Film.builder()
                .id(null)
                .name("Фильм №2")
                .description("Описание фильма №2")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        Long film2Id = controller.create(film2).getId();
        Film film3 = Film.builder()
                .id(null)
                .name("Фильм №3")
                .description("Описание фильма №3")
                .releaseDate(LocalDate.now())
                .duration(Duration.ofMinutes(90))
                .build();
        Long film3Id = controller.create(film3).getId();

        User user1 = User.builder()
                .id(null)
                .name("User 1")
                .email("user@ya.ru")
                .login("userLogin1")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user1Id = userStorage.create(user1).getId();
        User user2 = User.builder()
                .id(null)
                .name("User 2")
                .email("user2@ya.ru")
                .login("userLogin2")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user2Id = userStorage.create(user2).getId();
        User user3 = User.builder()
                .id(null)
                .name("User 3")
                .email("user3@ya.ru")
                .login("userLogin3")
                .birthday(LocalDate.of(2000, 2, 20))
                .build();
        Long user3Id = userStorage.create(user3).getId();

        controller.addLike(film2Id, user1Id);
        controller.addLike(film2Id, user2Id);
        controller.addLike(film2Id, user3Id);

        controller.addLike(film3Id, user1Id);
        controller.addLike(film3Id, user2Id);


        ArrayList<Film> responseEntity = new ArrayList<>(controller.getPopular(1L));
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(film2Id, responseEntity.get(0).getId());
    }
}