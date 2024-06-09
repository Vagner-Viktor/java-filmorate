package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmDirector;

import java.util.Collection;

@Slf4j
@Component
@Primary
public class FilmDirectorDBStorage extends BaseDbStorage<FilmDirector> implements FilmDirectorStorage {
    private static final String DIRECTORS_FIND_BY_FILM_ID_QUERY = """
            SELECT
                fd."film_id",
                d."director_id",
                d."name"
            FROM "films_director" AS fd
            JOIN "directors" AS d ON fd."director_id" = d."director_id"
            WHERE "film_id" IN (%s);
            """;

    public FilmDirectorDBStorage(JdbcTemplate jdbc, RowMapper<FilmDirector> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<FilmDirector> findDirectorsOfFilms(String filmsId) {
        return findMany(
                String.format(DIRECTORS_FIND_BY_FILM_ID_QUERY, filmsId)
        );
    }
}
