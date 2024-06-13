package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchType;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private static final String BY_DIRECTOR = "director";
    private static final String BY_TITLE = "title";

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        validate(film);
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        validate(newFilm);
        return filmStorage.update(newFilm);
    }

    public void delete(Long id) {
        filmStorage.delete(id);
    }

    public Film addLike(Long id, Long userId) {
        return filmStorage.addLike(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        return filmStorage.deleteLike(id, userId);
    }

    public Collection<Film> getPopular(Long count, Long genreId, int year) {
        return filmStorage.getPopular(count, genreId, year);
    }

    public Collection<Film> searchFilms(String query, List<String> by) {
        SearchType searchType = getSearchType(by);
        return filmStorage.searchFilms(query, searchType);
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

    public Collection<Film> getFilmsByDirector(Long id, String sortBy) {
        return filmStorage.getFilmsByDirector(id, sortBy);
    }

    public Collection<Film> getRecommendedFilmsForUser(Long id) {
        return filmStorage.getRecommendedFilmsForUser(id);
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException(String.format("Пользователь с id = %s не существует.", userId));
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException(String.format("Пользователь с id = %s не существует.", friendId));
        return filmStorage.getCommonFilms(userId, friendId);
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
    }
}
