package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

@Component
public class UserFeedRowMapper implements RowMapper<UserFeed> {
    @Override
    public UserFeed mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserFeed userFeed = new UserFeed();
        userFeed.setEventId(rs.getLong("user_event_id"));
        userFeed.setTimestamp(Instant.ofEpochMilli(rs.getLong("timestamp")));
        userFeed.setUserId(rs.getLong("user_id"));
        userFeed.setEntityId(rs.getLong("entity_id"));
        userFeed.setEventType(rs.getString("event_type_name"));
        userFeed.setOperation(rs.getString("operation_type_name"));
        return userFeed;
    }
}
