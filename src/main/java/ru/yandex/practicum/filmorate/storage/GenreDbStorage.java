package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Slf4j
@Component
@Primary
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {
    private static final String GENRES_FIND_ALL_QUERY = """
            SELECT *
            FROM "genres";
            """;
    private static final String GENRES_FIND_BY_ID_QUERY = """
            SELECT *
            FROM "genres"
            WHERE "genre_id" = ?;
            """;

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> findAll() {
        log.info("Получение списка жанров");
        return findMany(GENRES_FIND_ALL_QUERY);
    }

    @Override
    public Genre findById(int id) {
        log.info("Получение жанра с id = {}", id);
        return findOne(
                GENRES_FIND_BY_ID_QUERY,
                id
        ).orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден!"));
    }

    public boolean isGenresExists(Collection<Genre> genres) {
        for (Genre genre : genres) {
            if (!isGenreExists(genre.getId()))
                throw new ValidationException("Жанр с id = " + genre.getId() + " не найден!");
        }
        return true;
    }

    public boolean isGenreExists(int id) {
        return findOne(
                GENRES_FIND_BY_ID_QUERY,
                id).isPresent();
    }
}
