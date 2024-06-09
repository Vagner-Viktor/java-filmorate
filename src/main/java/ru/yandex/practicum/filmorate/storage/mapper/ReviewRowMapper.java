package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.constant.Feedback;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getLong("review_id"))
                .filmId(rs.getLong("film_id"))
                .userId(rs.getLong("user_id"))
                .content(rs.getString("content"))
                .feedback(Feedback.valueOf(rs.getString("feedback")))
                .evaluation(rs.getInt("evaluation"))
                .build();
    }
}
