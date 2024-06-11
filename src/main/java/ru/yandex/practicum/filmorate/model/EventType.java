package ru.yandex.practicum.filmorate.model;

public enum EventType {
    LIKE(1),
    REVIEW(2),
    FRIEND(3);
    private final int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
