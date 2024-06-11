package ru.yandex.practicum.filmorate.model;

public enum OperationType {
    REMOVE(1),
    ADD(2),
    UPDATE(3);
    private final int value;

    OperationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}