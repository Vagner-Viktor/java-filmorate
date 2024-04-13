package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.FilmsRepository;
import ru.yandex.practicum.filmorate.model.film.InMemFilmsRepository;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmsRepository filmsRepository = new InMemFilmsRepository();

    @GetMapping
    public Collection<Film> findAll() {
        return filmsRepository.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmsRepository.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return filmsRepository.update(newFilm);
    }

}
