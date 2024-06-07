package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Component
@Slf4j
@Primary
public class DirectorDbStorage extends BaseDbStorage<Director> implements DirectorStorage { // как поправишь вынеси константы

    public DirectorDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Director> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public List<Director> getAllDirectors() {
        log.info("Получение списка режиссеров");
        String sqlQuery = """
                SELECT *
                FROM "directors"
                """;

        return findMany(sqlQuery);
    }

    @Override
    public Director getDirectorById(Long id) {
        log.info("Получение режиссера по id = {}", id);
        String sqlQuery = """
                          SELECT *
                          FROM "directors"
                          WHERE "director_id" = ?
                          """;

        return findOne(sqlQuery, id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + id + " не найден!"));
    }

    @Override
    public Director addDirector(Director director) {
        String sqlQuery = """
                          INSERT INTO "directors" ("name")
                          VALUES (?);
                          """;

        Long id = insertGetKey(sqlQuery, director.getName());
        director.setId(id);

        log.info("Режиссер {} добавлен в список с id = {}", director.getName(), director.getId());

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = """
                          UPDATE "directors"
                          SET "name" = ?
                          WHERE "director_id" = ?;
                          """;

        if (director.getId() <= 0) {
            throw new ConditionsNotMetException("Id фильма должен быть указан");
        }

        if (checkDirectorExists(director.getId())) {
            update(sqlQuery, director.getName(), director.getId());

            log.info("Режиссер с id = {} обновлен", director.getId());
            return director;
        } else {
            throw new NotFoundException("Режиссер с id = " + director.getId() + " не найден");
        }
    }

    @Override
    public Long deleteDirector(Long id) {
        String sqlQuery = """
                          DELETE FROM "directors"
                          WHERE "director_id" = ?
                          """;

        if (checkDirectorExists(id)) {
            delete(sqlQuery, id);
            log.info("Режиссер с id = {} удален", id);
            return id;
        } else {
            throw new NotFoundException("Режиссер с id = " + id + " не найден");
        }
    }

//    public boolean checkDirectorExists(List<Director> directors) {
//        for (Director director : directors) {
//            if (!checkDirectorExists(director.getId()))
//                throw new ValidationException("Директор с id = " + director.getId() + " не найден!");
//        }
//        return true;
//    }

    public boolean checkDirectorExists(long id) {
        String sqlQuery = """
                          SELECT *
                          FROM "directors"
                          WHERE "director_id" = ?
                          """;

        return findOne(
                sqlQuery,
                id).isPresent();
    }
}
