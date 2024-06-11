package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.Collection;

@Slf4j
@Component
public class UserFeedDBStorage extends BaseDbStorage<UserFeed> implements UserFeedStorage {
    private static final String USER_FEEDS_FIND_BY_USER_ID = """
            SELECT
                ue."user_event_id" AS "user_event_id",
                ue."timestamp" AS "timestamp",
                ue."user_id" AS "user_id",
                ue."entity_id" AS "entity_id",
                et."name" AS "event_type_name",
                ot."name" AS "operation_type_name"
            FROM "user_events" AS ue
            LEFT JOIN "event_types" AS et ON et."event_type_id" = ue."event_type_id"
            LEFT JOIN "operation_types" AS ot ON  ot."operation_type_id" = ue."operation_type_id"
            WHERE ue."user_id" = ?;
            """;
    private static final String USER_FEEDS_INSERT_QUERY = """
            INSERT INTO "user_events" ("timestamp", "user_id", "entity_id", "event_type_id", "operation_type_id")
                        VALUES (?, ?, ?, ?, ?);
            """;

    public UserFeedDBStorage(JdbcTemplate jdbc, RowMapper<UserFeed> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public UserFeed create(UserFeed userFeed) {
        Long id = insertGetKey(USER_FEEDS_INSERT_QUERY,
                userFeed.getTimestamp().toEpochMilli(),
                userFeed.getUserId(),
                userFeed.getEntityId(),
                EventType.valueOf(userFeed.getEventType()).getValue(),
                OperationType.valueOf(userFeed.getOperation()).getValue());
        userFeed.setEventId(id);
        return userFeed;
    }

    @Override
    public Collection<UserFeed> findUserFeeds(Long id) {
        return findMany(USER_FEEDS_FIND_BY_USER_ID, id);
    }
}