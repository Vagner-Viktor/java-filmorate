package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService service;

    @Autowired
    public FilmController(FilmService service) {
        this.service = service;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return service.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return service.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return service.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        return service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        return service.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(
            @RequestParam(defaultValue = "10", required = false) Long count) {
        return service.getPopular(count);
    }

}
