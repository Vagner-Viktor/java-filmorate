package ru.yandex.practicum.filmorate.model;

public enum RatingMPA {
    G("G"),
    PG("PG"),
    PG13("PG-13"),
    R("R"),
    NC17("NC-17")
    ;

    private final String text;

    RatingMPA(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
