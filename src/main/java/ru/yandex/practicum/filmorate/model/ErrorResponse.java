package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

public class ErrorResponse {
    @Getter
    private String error;
    @Getter
    private String description;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }
}
