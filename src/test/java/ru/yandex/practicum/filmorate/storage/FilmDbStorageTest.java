package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
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
        FilmDirectorDBStorage.class,
        UserFeedDBStorage.class,
        ReviewDbStorage.class,
        UsabilityStateDbStorage.class})
@ComponentScan(basePackages = {"ru.yandex.practicum.filmorate.storage.mapper"})
class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @AllArgsConstructor
    static class ExpectedViolation {
        String propertyPath;
        String message;
    }

    public Film getTestFilm(int id) {
        switch (id) {
            case 1:
                Film film = Film.builder()
                        .id(null)
                        .name("Фильм №1")
                        .description("Описание фильма №1")
                        .releaseDate(LocalDate.now())
                        .duration(Duration.ofMinutes(90))
                        .mpa(new Mpa(1, "G"))
                        .genres(List.of(
                                new Genre(1, "Комедия"),
                                new Genre(2, "Драма")))
                        .build();
                return film;
            case 2:
                Film film2 = Film.builder()
                        .id(null)
                        .name("Фильм №2")
                        .description("Описание фильма №2")
                        .releaseDate(LocalDate.now().minusYears(1))
                        .duration(Duration.ofMinutes(90))
                        .mpa(new Mpa(2, "PG"))
                        .genres(List.of(
                                new Genre(3, "Мультфильм")))
                        .build();
                return film2;
            case 3:
                Film film3 = Film.builder()
                        .id(null)
                        .name("Фильм №3")
                        .description("Описание фильма №3")
                        .releaseDate(LocalDate.now().minusMonths(3))
                        .duration(Duration.ofMinutes(50))
                        .mpa(new Mpa(3, "PG-13"))
                        .genres(List.of(
                                new Genre(4, "Триллер")))
                        .build();
                return film3;
            default:
                return null;
        }
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
        Film film = getTestFilm(1);
        filmDbStorage.create(film);
        Film film2 = getTestFilm(2);
        filmDbStorage.create(film2);

        Collection<Film> responseEntity = filmDbStorage.findAll();
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
    }

    @Test
    void create() {
        Film film = getTestFilm(1);
        filmDbStorage.create(film);
        Collection<Film> responseEntity = filmDbStorage.findAll();
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
        Film film = getTestFilm(1);
        filmDbStorage.create(film);
        Collection<Film> responseEntity = filmDbStorage.findAll();
        final Film newFilm = getTestFilm(2);
        newFilm.setId(responseEntity.iterator().next().getId());
        filmDbStorage.update(newFilm);
        responseEntity = filmDbStorage.findAll();

        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(newFilm.getName(), responseEntity.iterator().next().getName());
        assertEquals(newFilm.getDescription(), responseEntity.iterator().next().getDescription());
        assertEquals(newFilm.getReleaseDate(), responseEntity.iterator().next().getReleaseDate());
        assertEquals(newFilm.getDuration(), responseEntity.iterator().next().getDuration());
    }

    @Test
    void createNullNameFilm() {
        Film film = getTestFilm(1);
        film.setName(null);
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
        Film film = getTestFilm(1);
        film.setDescription("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова, который задолжал им деньги, а именно 20 миллионов. о Куглов, который за время «своего отсутствия», стал кандидатом Коломбани.");
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
        Film film = getTestFilm(1);
        film.setReleaseDate(LocalDate.of(1895, 1, releaseDay));
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            Exception exception = assertThrows(
                    ValidationException.class,
                    () -> filmDbStorage.create(film)
            );
            assertEquals(
                    "Дата релиза не может быть раньше 28 декабря 1895 года!",
                    exception.getMessage()
            );
        } else {
            filmDbStorage.create(film);
            Collection<Film> responseEntity = filmDbStorage.findAll();
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
        Film film = getTestFilm(1);
        film.setDuration(Duration.ofMinutes(-90));
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
        Film film = getTestFilm(1);
        Long filmId = filmDbStorage.create(film).getId();

        User user = getTestUser(1);
        Long user1Id = userDbStorage.create(user).getId();

        filmDbStorage.addLike(filmId, user1Id);

        ArrayList<Film> responseEntity = new ArrayList<>(filmDbStorage.findAll());
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(1, responseEntity.get(0).getLikesCount());
        assertTrue(responseEntity.get(0).getLikes().contains(user1Id));
    }

    @Test
    void deleteLike() {
        Film film = getTestFilm(1);
        Long filmId = filmDbStorage.create(film).getId();

        User user = getTestUser(1);
        Long user1Id = userDbStorage.create(user).getId();

        filmDbStorage.addLike(filmId, user1Id);
        filmDbStorage.deleteLike(filmId, user1Id);

        ArrayList<Film> responseEntity = new ArrayList<>(filmDbStorage.findAll());
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(0, responseEntity.get(0).getLikesCount());
    }

    @Test
    void getPopularCount10() {
        Film film1 = getTestFilm(1);
        Long film1Id = filmDbStorage.create(film1).getId();
        Film film2 = getTestFilm(2);
        Long film2Id = filmDbStorage.create(film2).getId();
        Film film3 = getTestFilm(3);
        Long film3Id = filmDbStorage.create(film3).getId();

        User user1 = getTestUser(1);
        Long user1Id = userDbStorage.create(user1).getId();
        User user2 = getTestUser(2);
        Long user2Id = userDbStorage.create(user2).getId();
        User user3 = getTestUser(3);
        Long user3Id = userDbStorage.create(user3).getId();

        filmDbStorage.addLike(film2Id, user1Id);
        filmDbStorage.addLike(film2Id, user2Id);
        filmDbStorage.addLike(film2Id, user3Id);

        filmDbStorage.addLike(film3Id, user1Id);
        filmDbStorage.addLike(film3Id, user2Id);


        ArrayList<Film> responseEntity = new ArrayList<>(filmDbStorage.getPopular(10L, 0L, 0));
        assertNotNull(responseEntity);
        assertEquals(3, responseEntity.size());
        assertEquals(film2Id, responseEntity.get(0).getId());
        assertEquals(film3Id, responseEntity.get(1).getId());
        assertEquals(film1Id, responseEntity.get(2).getId());
    }

    @Test
    void getPopularCount1() {
        Film film1 = getTestFilm(1);
        Long film1Id = filmDbStorage.create(film1).getId();
        Film film2 = getTestFilm(2);
        Long film2Id = filmDbStorage.create(film2).getId();
        Film film3 = getTestFilm(3);
        Long film3Id = filmDbStorage.create(film3).getId();

        User user1 = getTestUser(1);
        Long user1Id = userDbStorage.create(user1).getId();
        User user2 = getTestUser(2);
        Long user2Id = userDbStorage.create(user2).getId();
        User user3 = getTestUser(3);
        Long user3Id = userDbStorage.create(user3).getId();

        filmDbStorage.addLike(film2Id, user1Id);
        filmDbStorage.addLike(film2Id, user2Id);
        filmDbStorage.addLike(film2Id, user3Id);

        filmDbStorage.addLike(film3Id, user1Id);
        filmDbStorage.addLike(film3Id, user2Id);


        ArrayList<Film> responseEntity = new ArrayList<>(filmDbStorage.getPopular(1L, 0L, 0));
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertEquals(film2Id, responseEntity.get(0).getId());
    }
}