package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final FilmDirectorStorage filmDirectorStorage;

    private static final String FILMS_FIND_ALL_QUERY = """
            SELECT *
            FROM "films" AS f
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id";
            """;
    private static final String FILMS_INSERT_QUERY = """
            INSERT INTO "films" ("name" , "description" , "release_date" , "duration", "mpa_id")
                        VALUES (?, ?, ?, ?, ?);
            """;
    private static final String FILMS_UPDATE_QUERY = """
            UPDATE "films"
            SET "name" = ?,
                "description" = ?,
                "release_date" = ?,
                "duration" = ?,
                "mpa_id" = ?
            WHERE "film_id" = ?;
            """;
    private static final String FILMS_FIND_BY_ID_QUERY = """
            SELECT *
            FROM "films" AS f
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            WHERE f."film_id" = ?;
            """;
    private static final String FILMS_ADD_LIKE_QUERY = """
            INSERT INTO "likes" ("film_id" , "user_id")
                        VALUES (?, ?);
            """;
    private static final String FILMS_DELETE_LIKE_QUERY = """
            DELETE FROM "likes"
            WHERE "film_id" = ?
                AND "user_id" = ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
            COUNT(l."film_id") AS count
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_genre" AS fg ON fg."film_id" = f."film_id"
            LEFT JOIN "genres" AS g ON g."genre_id" = fg."genre_id"
            GROUP BY "film_id"
            ORDER BY count DESC
            LIMIT ?;
            """;

    private static final String FILMS_GET_POPULAR_QUERY_WITH_GENRE = """
            SELECT
                        f."film_id" AS "film_id",
                        f."name" AS "name",
                        f."description" AS "description",
                        f."release_date" AS "release_date",
                        f."duration" AS "duration",
                        r."mpa_id" AS "mpa_id",
                        r."mpa" AS "mpa",
                        COUNT(l."film_id") AS count
                    FROM "films" AS f
                    LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
                    LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
                    LEFT JOIN "films_genre" AS fg ON fg."film_id" = f."film_id"
                    LEFT JOIN "genres" AS g ON g."genre_id" = fg."genre_id"
                    WHERE fg."genre_id" = ?
                    GROUP BY "film_id"
                    ORDER BY count DESC
                    LIMIT ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY_WITH_YEAR = """
            SELECT
                        f."film_id" AS "film_id",
                        f."name" AS "name",
                        f."description" AS "description",
                        f."release_date" AS "release_date",
                        f."duration" AS "duration",
                        r."mpa_id" AS "mpa_id",
                        r."mpa" AS "mpa",
                        COUNT(l."film_id") AS count
                    FROM "films" AS f
                    LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
                    LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
                    LEFT JOIN "films_genre" AS fg ON fg."film_id" = f."film_id"
                    LEFT JOIN "genres" AS g ON g."genre_id" = fg."genre_id"
                    WHERE EXTRACT(YEAR FROM f."release_date") = ?
                    GROUP BY "film_id"
                    ORDER BY count DESC
                    LIMIT ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY_WITH_YEAR_AND_GENRE = """
            SELECT
                        f."film_id" AS "film_id",
                        f."name" AS "name",
                        f."description" AS "description",
                        f."release_date" AS "release_date",
                        f."duration" AS "duration",
                        r."mpa_id" AS "mpa_id",
                        r."mpa" AS "mpa",
                        COUNT(l."film_id") AS count
                    FROM "films" AS f
                    LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
                    LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
                    LEFT JOIN "films_genre" AS fg ON fg."film_id" = f."film_id"
                    LEFT JOIN "genres" AS g ON g."genre_id" = fg."genre_id"
                    WHERE EXTRACT(YEAR FROM f."release_date") = ? AND fg."genre_id" = ?
                    GROUP BY "film_id"
                    ORDER BY count DESC
                    LIMIT ?;
            """;
    private static final String FILMS_DELETE_FILMS_GENRE_QUERY = """
            DELETE FROM "films_genre"
            WHERE "film_id" = ?;
            """;
    private static final String FILMS_INSERT_FILMS_GENRE_QUERY = """
            INSERT INTO "films_genre" ("film_id", "genre_id")
                VALUES (?, ?);
            """;
    private static final String FILMS_SEARCH_BY_TITLE = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
            COUNT(l."film_id") AS count
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            WHERE LOWER(f."name") LIKE LOWER('%' || ? || '%')
            GROUP BY f."name", f."film_id"
            ORDER BY count DESC;
            """;
    private static final String FILMS_SEARCH_BY_DIRECTOR = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
            COUNT(l."film_id") AS count
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_director" AS fd ON f."film_id" = fd."film_id"
            LEFT JOIN "directors" AS d ON fd."director_id" = d."director_id"
            WHERE LOWER(d."name") LIKE LOWER('%' || ? || '%')
            GROUP BY f."name", f."film_id"
            ORDER BY count DESC;
            """;
    private static final String FILMS_SEARCH_BY_TITLE_AND_DIRECTOR = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
            COUNT(l."film_id") AS count
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_director" AS fd ON f."film_id" = fd."film_id"
            LEFT JOIN "directors" AS d ON fd."director_id" = d."director_id"
            WHERE LOWER(d."name") LIKE LOWER('%' || ? || '%')
                OR LOWER(f."name") LIKE LOWER('%' || ? || '%')
            GROUP BY f."name", f."film_id"
            ORDER BY count DESC;
            """;
    private static final String FILMS_DELETE = """
            DELETE FROM "films"
            WHERE "film_id" = ?;
            """;

    private static final String FILMS_INSERT_FILMS_DIRECTORS_QUERY = """
            INSERT INTO "films_director" ("film_id", "director_id")
                VALUES (?, ?);
            """;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, UserStorage userStorage, GenreStorage genreStorage, MpaStorage mpaStorage, FilmLikeStorage likeStorage, FilmGenreStorage filmGenreStorage, FilmDirectorStorage filmDirectorStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.filmLikeStorage = likeStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.filmDirectorStorage = filmDirectorStorage;
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Получение списка фильмов");
        Collection<Film> films = findMany(FILMS_FIND_ALL_QUERY);
        setFilmsGenres(films);
        setFilmsLikes(films);
        return films;
    }

    @Override
    public Film findById(Long id) {
        log.info("Получение фильма с id = {}", id);
        Collection<Film> films = findMany(FILMS_FIND_BY_ID_QUERY, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
        setFilmsGenres(films);
        setFilmsLikes(films);
        return films.iterator().next();
    }

    @Override
    public Film create(Film film) {
        validate(film);
        long id = insertGetKey(
                FILMS_INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        for (Genre genre : film.getGenres()) {
            insert(
                    FILMS_INSERT_FILMS_GENRE_QUERY,
                    film.getId(),
                    genre.getId()
            );
        }
        for (Director director : film.getDirectors()) {
            insert(
                    FILMS_INSERT_FILMS_DIRECTORS_QUERY,
                    film.getId(),
                    director.getId()
            );
        }

        log.info("Фильм {} добавлен в список с id = {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ConditionsNotMetException("Id фильма должен быть указан");
        }
        if (checkFilmExists(film.getId())) {
            validate(film);
            update(
                    FILMS_UPDATE_QUERY,
                    film.getName(),
                    film.getDescription(),
                    java.sql.Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId()
            );
            delete(
                    FILMS_DELETE_FILMS_GENRE_QUERY,
                    film.getId()
            );
            for (Genre genre : film.getGenres()) {
                insert(
                        FILMS_INSERT_FILMS_GENRE_QUERY,
                        film.getId(),
                        genre.getId()
                );
            }
            for (Director director : film.getDirectors()) {
                insert(
                        FILMS_INSERT_FILMS_DIRECTORS_QUERY,
                        film.getId(),
                        director.getId()
                );
            }
            log.info("Фильм с id = {} обновлен", film.getId());
            return film;
        }
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    // удаление фильма по id, модифицировал связи в schema, при удалении фильма удаляются зависимые записи по id
    @Override
    public void delete(Long id) {
        if (!checkFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        delete(FILMS_DELETE,id);
        log.info("Фильм с id = {} удален", id);
    }

    @Override
    public Film addLike(Long id, Long userId) {
        if (!checkFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        Film film = findOne(
                FILMS_FIND_BY_ID_QUERY,
                id
        ).orElse(null);
        insert(
                FILMS_ADD_LIKE_QUERY,
                id,
                userId
        );
        film.addLike(userId);
        log.info("Пользователь с id = {} поставил лайк фильму id = {}", userId, id);
        return film;
    }

    @Override
    public Film deleteLike(Long id, Long userId) {
        if (!checkFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        if (!userStorage.checkUserExists(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        Film film = findOne(
                FILMS_FIND_BY_ID_QUERY,
                id
        ).orElse(null);
        delete(
                FILMS_DELETE_LIKE_QUERY,
                id,
                userId
        );
        film.deleteLike(userId);
        log.info("Пользователь с id = {} удалил лайк фильму id = {}", userId, id);
        return film;
    }

    @Override
    public Collection<Film> getPopular(Long count, Long genreId, int year) {
        if (count <= 0) throw new ValidationException("Параметр count должен быть больше 0");
        log.info("Получение списка {} популярных фильмов", count);
        //если ищем по count и year
        if (genreId == 0L && year >= 1) {
            return findMany(
                    FILMS_GET_POPULAR_QUERY_WITH_YEAR,
                    year, count);
        }
        //если ищем по count и genre
        if (genreId >= 1L && year == 0) {
            return findMany(
                    FILMS_GET_POPULAR_QUERY_WITH_GENRE,
                    genreId, count);
        }
        //если ищем по count, genre и year
        if (genreId >= 1L && year >= 1) {
            return findMany(
                    FILMS_GET_POPULAR_QUERY_WITH_YEAR_AND_GENRE,
                    year, genreId, count);
        }
        //только count
        return findMany(
                    FILMS_GET_POPULAR_QUERY,
                    count);
    }

    public boolean checkFilmExists(Long id) {
        return findOne(
                FILMS_FIND_BY_ID_QUERY,
                id).isPresent();
    }

    public Collection<Film> searchFilms(String query, SearchType searchType) {
        log.info("Получение фильмов по значению = {}", query);
        Collection<Film> films = switch (searchType) {
            case TITLE_AND_DIRECTOR -> findMany(FILMS_SEARCH_BY_TITLE_AND_DIRECTOR, query, query);
            case DIRECTOR -> findMany(FILMS_SEARCH_BY_DIRECTOR, query);
            default -> findMany(FILMS_SEARCH_BY_TITLE, query);
        };
        setFilmsGenres(films);
        setFilmsLikes(films);
        return films;
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

    private void setFilmsLikes(Collection<Film> films) {
        String filmsId = films.stream()
                .map(film -> {
                    return film.getId().toString();
                })
                .collect(Collectors.joining(", "));
        Collection<FilmLike> filmLikes = filmLikeStorage.findLikesOfFilms(filmsId);
        for (Film film : films) {
            film.setLikes(filmLikes.stream()
                    .filter(filmLike -> film.getId() == filmLike.getFilmId())
                    .map(filmLike -> filmLike.getUserId())
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

    private boolean validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
        genreStorage.checkGenresExists(film.getGenres());
        film.setGenres(new HashSet<>(film.getGenres()));
        if (!mpaStorage.checkMpaExists(film.getMpa().getId())) {
            throw new ValidationException("Рейтинг MPA с id = " + film.getMpa().getId() + " не найден!");
        }
        return true;
    }
}
