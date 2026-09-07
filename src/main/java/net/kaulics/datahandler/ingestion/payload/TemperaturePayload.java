package net.kaulics.datahandler.ingestion.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record TemperaturePayload(
        @NotBlank String deviceId,
        @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING) Instant timestamp,
        double celsius) {
}
