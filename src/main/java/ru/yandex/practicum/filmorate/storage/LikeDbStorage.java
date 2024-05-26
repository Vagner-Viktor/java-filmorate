package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
@Primary
public class LikeDbStorage extends BaseDbStorage<Long> implements LikeStorage {
    private static final String LIKES_FIND_BY_FILM_ID_QUERY = """
            SELECT *
            FROM "likes"
            WHERE "film_id" = ?;
            """;

    public LikeDbStorage(JdbcTemplate jdbc, RowMapper<Long> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Long> findLikesOfFilm(Long id) {
        log.info("Получение списка лайков для фильма с id = {}", id);
        return findMany(
                LIKES_FIND_BY_FILM_ID_QUERY,
                id
        );
    }
}
