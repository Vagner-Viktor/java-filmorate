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

import java.sql.Date;
import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Component
@Primary
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

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
            MERGE INTO "likes" ("film_id" , "user_id", "mark")
                        VALUES (?, ?, ?);
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
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
            GROUP BY "film_id"
            ORDER BY avg DESC
            LIMIT ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY_BY_GENRE = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_genre" AS fg ON fg."film_id" = f."film_id"
            WHERE fg."genre_id" = ?
            GROUP BY "film_id"
            ORDER BY avg DESC
            LIMIT ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY_BY_YEAR = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
            WHERE EXTRACT(YEAR FROM f."release_date") = ?
            GROUP BY "film_id"
            ORDER BY avg DESC
            LIMIT ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY_BY_YEAR_AND_GENRE = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_genre" AS fg ON fg."film_id" = f."film_id"
            WHERE EXTRACT(YEAR FROM f."release_date") = ? AND fg."genre_id" = ?
            GROUP BY "film_id"
            ORDER BY avg DESC
            LIMIT ?;
            """;
    private static final String FILMS_DELETE_FILMS_GENRE_QUERY = """
            DELETE FROM "films_genre"
            WHERE "film_id" = ?;
            """;
    private static final String FILMS_INSERT_FILMS_GENRE_QUERY = """
            MERGE INTO "films_genre" ("film_id", "genre_id")
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
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            WHERE LOWER(f."name") LIKE LOWER('%' || ? || '%')
            GROUP BY f."name", f."film_id"
            ORDER BY avg DESC, "film_id";
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
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_director" AS fd ON f."film_id" = fd."film_id"
            LEFT JOIN "directors" AS d ON fd."director_id" = d."director_id"
            WHERE LOWER(d."name") LIKE LOWER('%' || ? || '%')
            GROUP BY f."name", f."film_id"
            ORDER BY avg DESC, "film_id";
            """;
    private static final String FILMS_DELETE_FILMS_DIRECTOR_QUERY = """
            DELETE FROM "films_director"
            WHERE "film_id" = ?;
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
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_director" AS fd ON f."film_id" = fd."film_id"
            LEFT JOIN "directors" AS d ON fd."director_id" = d."director_id"
            WHERE LOWER(d."name") LIKE LOWER('%' || ? || '%')
                OR LOWER(f."name") LIKE LOWER('%' || ? || '%')
            GROUP BY f."name", f."film_id"
            ORDER BY avg DESC, "film_id";
            """;
    private static final String FILMS_DELETE = """
            DELETE FROM "films"
            WHERE "film_id" = ?;
            """;
    private static final String FILMS_INSERT_FILMS_DIRECTORS_QUERY = """
            MERGE INTO "films_director" ("film_id", "director_id")
                VALUES (?, ?);
            """;
    private static final String GET_FILMS_BY_DIRECTOR_ID_SORTED_BY_DATE = """
            SELECT * FROM "films" AS f
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_director" AS fd ON f."film_id" = fd."film_id"
            WHERE fd."director_id" = ?
            GROUP BY f."film_id"
            ORDER BY f."release_date";
            """;
    private static final String GET_FILMS_BY_DIRECTOR_ID_SORTED_BY_LIKES = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            LEFT JOIN "films_director" AS fd ON f."film_id" = fd."film_id"
            WHERE fd."director_id" = ?
            GROUP BY f."film_id"
            ORDER BY avg DESC;
            """;
    private static final String GET_FILMS_RECOMMENDATIONS = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa"
            FROM "films" f
            LEFT JOIN "mpas" AS r ON f."mpa_id" = r."mpa_id"
            LEFT JOIN "likes" l ON f."film_id" = l."film_id"
            WHERE l."user_id" IN (
                SELECT "user_id"
                FROM "likes"
                WHERE NOT "user_id" = ? AND "film_id" IN (
                    SELECT "film_id"
                    FROM "likes"
                    WHERE "user_id" = ?
                    )
                GROUP BY "user_id"
                ORDER BY COUNT("film_id") DESC
                LIMIT 1
                ) AND NOT l."film_id" IN (
                SELECT "film_id"
                FROM "likes"
                WHERE "user_id" = ?
                )
            GROUP BY f."film_id"
            HAVING AVG(l."mark") >= 6
            LIMIT 1;
            """;

    private static final String GET_COMMON_FILMS = """
            SELECT
                f."film_id" AS "film_id",
                f."name" AS "name",
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa_id" AS "mpa_id",
                r."mpa" AS "mpa",
                AVG(l."mark") AS avg
            FROM "films" AS f
            LEFT JOIN "likes" l ON f."film_id" = l."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            WHERE f."film_id" IN (
                SELECT l1."film_id"
                FROM "likes" AS l1
                LEFT JOIN "likes" AS l2 ON l1."film_id" = l2."film_id"
                WHERE l1."user_id" = ? AND l2."user_id" = ?
            )
            GROUP BY f."film_id"
            ORDER BY avg, f."film_id"
            """;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Получение списка фильмов");
        return findMany(FILMS_FIND_ALL_QUERY);
    }

    @Override
    public Film findById(Long id) {
        log.info("Получение фильма с id = {}", id);
        return findOne(
                FILMS_FIND_BY_ID_QUERY,
                id).orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден!"));
    }

    @Override
    public Film create(Film film) {
        long id = insertGetKey(
                FILMS_INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        film.setGenres(film.getGenres().stream()
                .distinct()
                .sorted(Comparator.comparingInt(Genre::getId))
                .toList());
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
        if (isFilmExists(film.getId())) {
            update(
                    FILMS_UPDATE_QUERY,
                    film.getName(),
                    film.getDescription(),
                    Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId()
            );
            delete(
                    FILMS_DELETE_FILMS_GENRE_QUERY,
                    film.getId()
            );
            film.setGenres(film.getGenres().stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .toList());
            for (Genre genre : film.getGenres()) {
                insert(
                        FILMS_INSERT_FILMS_GENRE_QUERY,
                        film.getId(),
                        genre.getId()
                );
            }
            delete(
                    FILMS_DELETE_FILMS_DIRECTOR_QUERY,
                    film.getId()
            );
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
        if (!isFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        delete(FILMS_DELETE, id);
        log.info("Фильм с id = {} удален", id);
    }

    @Override
    public Film addLike(Long id, Long userId, Float mark) {
        if (!isFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        Film film = findOne(
                FILMS_FIND_BY_ID_QUERY,
                id
        ).orElse(null);
        insert(
                FILMS_ADD_LIKE_QUERY,
                id,
                userId,
                mark
        );
        film.addLike(new FilmLike(id, userId, mark));

        log.info("Пользователь с id = {} поставил лайк фильму id = {}", userId, id);
        return film;
    }

    @Override
    public Film deleteLike(Long id, Long userId) {
        if (!isFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        Film film = findOne(
                FILMS_FIND_BY_ID_QUERY,
                id
        ).orElse(null);
        delete(
                FILMS_DELETE_LIKE_QUERY,
                id,
                userId
        );
        film.deleteLike(new FilmLike(id, userId, 0.0f));
        log.info("Пользователь с id = {} удалил лайк фильму id = {}", userId, id);
        return film;
    }

    @Override
    public Collection<Film> getPopular(Long count, Long genreId, int year) {
        if (count <= 0) throw new ValidationException("Параметр count должен быть больше 0");
        log.info("Получение списка {} популярных фильмов", count);
        Collection<Film> films = null;

        //если ищем по avg и year
        if (genreId == 0L && year >= 1) {
            films = findMany(
                    FILMS_GET_POPULAR_QUERY_BY_YEAR,
                    year, count);
        }
        //если ищем по avg и genre
        if (genreId >= 1L && year == 0) {
            films = findMany(
                    FILMS_GET_POPULAR_QUERY_BY_GENRE,
                    genreId, count);
        }
        //если ищем по avg, genre и year
        if (genreId >= 1L && year >= 1) {
            films = findMany(
                    FILMS_GET_POPULAR_QUERY_BY_YEAR_AND_GENRE,
                    year, genreId, count);
        }
        //только avg
        if (films == null) {
            films = findMany(
                    FILMS_GET_POPULAR_QUERY,
                    count);
        }
        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirector(Long id, String sortBy) { // получаем sorted film list по likes или date
        switch (sortBy) {
            case "year" -> {
                return findMany(GET_FILMS_BY_DIRECTOR_ID_SORTED_BY_DATE, id);
            }
            case "likes" -> {
                return findMany(GET_FILMS_BY_DIRECTOR_ID_SORTED_BY_LIKES, id);
            }
            default -> throw new NotFoundException("Данный вид сортировки " + sortBy + " не найден");
        }
    }

    @Override
    public Collection<Film> getRecommendedFilmsForUser(Long id) {
        return findMany(GET_FILMS_RECOMMENDATIONS, id, id, id);
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return findMany(GET_COMMON_FILMS, userId, friendId);
    }

    @Override
    public boolean isFilmExists(Long id) {
        return findOne(
                FILMS_FIND_BY_ID_QUERY,
                id).isPresent();
    }

    public Collection<Film> searchFilms(String query, SearchType searchType) {
        log.info("Получение фильмов по значению = {}", query);
        switch (searchType) {
            case TITLE_AND_DIRECTOR -> {
                return findMany(FILMS_SEARCH_BY_TITLE_AND_DIRECTOR, query, query);
            }
            case DIRECTOR -> {
                return findMany(FILMS_SEARCH_BY_DIRECTOR, query);
            }
            default -> {
                return findMany(FILMS_SEARCH_BY_TITLE, query);
            }
        }
    }
}
