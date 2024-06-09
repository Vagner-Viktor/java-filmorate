package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService service;

    @GetMapping
    public Collection<Film> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return service.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return service.update(newFilm);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
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

    // пример запроса будет выглядить так GET /films/popular?count={limit}&genreId={genreId}&year={year}
    @GetMapping("/popular")
    public Collection<Film> getPopular(
            @RequestParam(defaultValue = "10", required = false) Long count,
            @RequestParam(defaultValue = "0", required = false) Long genreId,
            @RequestParam(defaultValue = "0", required = false) int year) {
        return service.getPopular(count, genreId, year);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(
            @RequestParam(defaultValue = "", required = false) String query,
            @RequestParam(defaultValue = "title", required = false) List<String> by) {
        return service.searchFilms(query, by);
    }
    @GetMapping("/director/{id}")
    public Collection<Film> getFilmsByDirector(@PathVariable Long id, @RequestParam String sortBy) {
        return service.getFilmsByDirector(id, sortBy);
    }
}
