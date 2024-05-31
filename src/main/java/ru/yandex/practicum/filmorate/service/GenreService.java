package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage storage;

    public Collection<Genre> findAll() {
        return storage.findAll();
    }

    public Genre findById(int id) {
        return storage.findById(id);
    }
}
