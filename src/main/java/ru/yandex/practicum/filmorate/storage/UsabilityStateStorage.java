package ru.yandex.practicum.filmorate.storage;

import java.util.Optional;

public interface UsabilityStateStorage {

    Optional<Integer> getCurrentState(Long reviewId, Long userId);
}
