package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorDbStorage directorDbStorage;
    private final FilmDirectorStorage filmDirectorStorage;
    private final UserFeedStorage userFeedStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private static final String BY_DIRECTOR = "director";
    private static final String BY_TITLE = "title";

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        setFilmsGenres(films);
        setFilmsDirectors(films);
        setFilmsLikes(films);
        return films;
    }

    public Film findById(Long id) {
        Collection<Film> films = List.of(filmStorage.findById(id));
        setFilmsGenres(films);
        setFilmsDirectors(films);
        setFilmsLikes(films);
        return films.iterator().next();
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

    public Film addLike(Long id, Long userId, Integer mark) {
        if (!userStorage.isUserExists(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(userId)
                .entityId(id)
                .timestamp(Instant.now())
                .eventType(EventType.LIKE.name())
                .operation(OperationType.ADD.name())
                .build());
        Film film = filmStorage.addLike(id, userId, mark);
        setFilmsLikes(List.of(film));
        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        if (!userStorage.isUserExists(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        userFeedStorage.create(UserFeed.builder()
                .eventId(null)
                .userId(userId)
                .entityId(id)
                .timestamp(Instant.now())
                .eventType(EventType.LIKE.name())
                .operation(OperationType.REMOVE.name())
                .build());
        Film film = filmStorage.deleteLike(id, userId);
        setFilmsLikes(List.of(film));
        return film;
    }

    public Collection<Film> getPopular(Long count, Long genreId, int year) {
        Collection<Film> films = filmStorage.getPopular(count, genreId, year);
        setFilmsGenres(films);
        setFilmsDirectors(films);
        setFilmsLikes(films);
        return films;
    }

    public Collection<Film> searchFilms(String query, List<String> by) {
        SearchType searchType = getSearchType(by);
        Collection<Film> films = filmStorage.searchFilms(query, searchType);
        setFilmsGenres(films);
        setFilmsDirectors(films);
        setFilmsLikes(films);
        return films;
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
        if (!directorDbStorage.isDirectorExists(id))
            throw new NotFoundException("Режисер с id = " + id + " не найден");
        log.info("Получение списка фильмов режиссера {} ", id);
        Collection<Film> films = filmStorage.getFilmsByDirector(id, sortBy);
        setFilmsGenres(films);
        setFilmsDirectors(films);
        setFilmsLikes(films);
        return films;
    }

    public Collection<Film> getRecommendedFilmsForUser(Long id) {
        Collection<Film> films = filmStorage.getRecommendedFilmsForUser(id);
        setFilmsGenres(films);
        setFilmsDirectors(films);
        setFilmsLikes(films);
        return films;
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        if (!userStorage.isUserExists(userId))
            throw new NotFoundException(String.format("Пользователь с id = %s не существует.", userId));
        if (!userStorage.isUserExists(userId))
            throw new NotFoundException(String.format("Пользователь с id = %s не существует.", friendId));
        Collection<Film> films = filmStorage.getCommonFilms(userId, friendId);
        setFilmsGenres(films);
        setFilmsDirectors(films);
        setFilmsLikes(films);
        return films;
    }

    private boolean validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
        genreStorage.checkGenresExists(film.getGenres());
        film.setGenres(new HashSet<>(film.getGenres()));
        if (!mpaStorage.isMpaExists(film.getMpa().getId())) {
            throw new ValidationException("Рейтинг MPA с id = " + film.getMpa().getId() + " не найден!");
        }
        return true;
    }

    private void setFilmsGenres(Collection<Film> films) {
        String filmsId = films.stream()
                .map(film -> {
                    return film.getId().toString();
                })
                .collect(Collectors.joining(", "));
        Collection<FilmGenre> filmGenres = filmGenreStorage.findGenresOfFilms(filmsId);
        for (Film film : films) {
            film.setGenres(filmGenres.stream()
                    .filter(filmGenre -> film.getId() == filmGenre.getFilmId())
                    .map(filmGenre -> new Genre(
                            filmGenre.getGenreId(),
                            filmGenre.getGenre())
                    )
                    .collect(Collectors.toList()));
        }
    }

    private void setFilmsDirectors(Collection<Film> films) {
        String filmsId = films.stream()
                .map(film -> {
                    return film.getId().toString();
                })
                .collect(Collectors.joining(", "));
        Collection<FilmDirector> filmDirectors = filmDirectorStorage.findDirectorsOfFilms(filmsId);
        for (Film film : films) {
            film.setDirectors(filmDirectors.stream()
                    .filter(filmDirector -> film.getId() == filmDirector.getFilmId())
                    .map(filmDirector -> new Director(
                            filmDirector.getDirectorId(),
                            filmDirector.getName())
                    )
                    .collect(Collectors.toList()));
        }
    }

    private void setFilmsLikes(Collection<Film> films) {
        String filmsId = films.stream()
                .map(film -> {
                    return film.getId().toString();
                })
                .collect(Collectors.joining(", "));
        Collection<FilmLike> filmLikes = filmLikeStorage.findLikesOfFilms(filmsId);
        for (Film film : films) {
            film.setLikes(filmLikes.stream()
                    .filter(filmLike -> film.getId().equals(filmLike.getFilmId()))
                    .toList());
        }
    }
}
