package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, without = JsonFormat.Feature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
    private Instant timestamp;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NotNull
    private Long entityId;

    @JsonProperty("timestamp")
    @Positive
    public long toEpochMilli() {
        return timestamp.toEpochMilli();
    }
}
