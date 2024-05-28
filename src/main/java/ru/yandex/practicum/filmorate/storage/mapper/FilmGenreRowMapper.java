package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmGenreRowMapper implements RowMapper<FilmGenre> {
    @Override
    public FilmGenre mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FilmGenre(
                rs.getLong("film_id"),
                rs.getInt("genre_id"),
                rs.getString("genre")
        );
    }
}
