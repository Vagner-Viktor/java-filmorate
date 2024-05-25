package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Slf4j
@Component
@Primary
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    private final UserStorage userStorage;
    private static final String FILMS_FIND_ALL_QUERY = """
            SELECT * 
            FROM "films" AS f
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id";
            """;
    private static final String FILMS_INSERT_QUERY = """
            INSERT INTO "films" ("name" , "description" , "release_date" , "duration")
                        VALUES (?, ?, ?, ?);
            """;
    private static final String FILMS_UPDATE_QUERY = """
            UPDATE "films" 
            SET "name" = ?, 
                "description" = ?, 
                "release_date" = ?, 
                "duration" = ? 
            WHERE "film_id" = ?;
            """;
    private static final String FILMS_FIND_BY_ID_QUERY = """
            SELECT * 
            FROM "films" AS f
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"            
            WHERE "film_id" = ?;
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
                f."name" AS name,
                f."description" AS "description",
                f."release_date" AS "release_date",
                f."duration" AS "duration",
                r."mpa" AS "mpa",
            COUNT(l."film_id") AS count
            FROM "films" AS f
            LEFT JOIN "likes" AS l ON l."film_id" = f."film_id"
            LEFT JOIN "mpas" AS r ON  f."mpa_id" = r."mpa_id"
            GROUP BY name
            ORDER BY count DESC
            LIMIT ?;
            """;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, UserStorage userStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Получение списка фильмов");
        return findMany(FILMS_FIND_ALL_QUERY);
    }

    @Override
    public Film create(Film film) {
        long id = insertGetKey(
                FILMS_INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration()
        );
        film.setId(id);
        log.info("Фильм {} добавлен в список с id = {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ConditionsNotMetException("Id фильма должен быть указан");
        }
        if (checkFilmExists(film.getId())) {
            update(
                    FILMS_UPDATE_QUERY,
                    film.getName(),
                    film.getDescription(),
                    java.sql.Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getId()
            );
            log.info("Фильм с id = {} обновлен", film.getId());
            return film;
        }
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
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
    public Collection<Film> getPopular(Long count) {
        if (count <= 0) throw new ValidationException("Параметр count должен быть больше 0");
        log.info("Получение списка {} популярных фильмов", count);
        return findMany(
                FILMS_GET_POPULAR_QUERY,
                count
        );
    }

    public boolean checkFilmExists(Long id) {
        return findOne(
                FILMS_FIND_BY_ID_QUERY,
                id).isPresent();
    }
}
