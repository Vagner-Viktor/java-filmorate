package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeed {
    private Long eventId;
    @NotNull
    private Long userId;
    @NotNull
    private Instant timestamp;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NotNull
    private Long entityId;

    @JsonProperty("timestamp")
    @Positive
    public long getTimestampEpochSecond() {
        return timestamp.getEpochSecond();
    }
}
