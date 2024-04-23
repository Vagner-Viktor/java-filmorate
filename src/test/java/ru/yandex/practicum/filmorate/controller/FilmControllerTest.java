package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {


    FilmStorage storage = new InMemoryFilmStorage();
    FilmService service = new FilmService(storage);
    FilmController controller = new FilmController(service);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @AllArgsConstructor
    static class ExpectedViolation {
        String propertyPath;
        String message;
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


}