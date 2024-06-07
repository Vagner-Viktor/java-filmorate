package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.List;

@Service
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    @Autowired
    public DirectorService(DirectorDbStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    public List<Director> getAllDirectors() {
        return directorDbStorage.getAllDirectors();
    }

    public Director getDirectorById(Long id) {
        return directorDbStorage.getDirectorById(id);
    }

    public Director addDirector(Director director) {
        return directorDbStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorDbStorage.updateDirector(director);
    }

    public Long deleteDirector(Long id) {
        return directorDbStorage.deleteDirector(id);
    }
}
