package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ValidationException;

public class JakartaValidationException extends ValidationException {
    public JakartaValidationException(String message) {
        super(message);
    }
}
