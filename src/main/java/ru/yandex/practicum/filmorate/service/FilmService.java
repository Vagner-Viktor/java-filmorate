package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchType;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage storage;
    private static final String BY_DIRECTOR = "director";
    private static final String BY_TITLE = "title";

    public Collection<Film> findAll() {
        return storage.findAll();
    }

    public Film findById(Long id) {
        return storage.findById(id);
    }

    public Film create(Film film) {
        validate(film);
        return storage.create(film);
    }

    public Film update(Film newFilm) {
        validate(newFilm);
        return storage.update(newFilm);
    }

    public Film addLike(Long id, Long userId) {
        return storage.addLike(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        return storage.deleteLike(id, userId);
    }

    public Collection<Film> getPopular(Long count) {
        return storage.getPopular(count);
    }

    public Collection<Film> searchFilms(String query, List<String> by) {
        SearchType searchType = getSearchType(by);
        return storage.searchFilms(query, searchType);
    }

    private SearchType getSearchType(List<String> by) {
        if ((by.size() == 2)
                && ((by.get(0).equals(BY_DIRECTOR) && (by.get(1).equals(BY_TITLE)))
                || (by.get(0).equals(BY_TITLE) && (by.get(1).equals(BY_DIRECTOR))))) {
            return SearchType.TITLE_AND_DIRECTOR;
        }
        if (by.size() == 1 && by.get(0).equals(BY_DIRECTOR)) {
            return SearchType.DIRECTOR;
        }
        return SearchType.TITLE;
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
    }
}
