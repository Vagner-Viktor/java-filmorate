package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

public class ErrorResponse {
    @Getter
    String error;
    @Getter
    String description;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }
}
