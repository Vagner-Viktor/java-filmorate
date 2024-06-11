package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    public Collection<Director> getAllDirectors() {
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
