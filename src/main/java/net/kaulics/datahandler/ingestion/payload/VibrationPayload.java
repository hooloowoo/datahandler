package net.kaulics.datahandler.ingestion.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;

public record VibrationPayload(
        @NotBlank String deviceId,
        @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING) Instant baseTimestamp,
        @Positive double sampleRateHz,
        @NotEmpty List<Double> samples) {
}
