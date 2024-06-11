package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@Primary
public class UsabilityStateDbStorage extends BaseDbStorage<Integer> implements UsabilityStateStorage {

    public UsabilityStateDbStorage(JdbcTemplate jdbc, RowMapper<Integer> mapper) {
        super(jdbc, mapper);
    }

    private static final String REQUEST_GET_USABILITY_STATE = """
            SELECT "usability_id"
            FROM "usability_reviews"
            WHERE "user_id" = ? AND "review_id" = ?
            """;

    public Optional<Integer> getCurrentState(Long reviewId, Long userId) {
        return findOne(REQUEST_GET_USABILITY_STATE, userId, reviewId);
    }

}
