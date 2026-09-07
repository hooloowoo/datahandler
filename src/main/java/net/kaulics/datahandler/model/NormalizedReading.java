package net.kaulics.datahandler.model;

import java.time.Instant;
import java.util.Map;

public record NormalizedReading(
        String deviceId,
        String deviceType,
        Instant eventTime,
        Instant ingestTime,
        double value,
        String unit,
        boolean late,
        Map<String, Object> attributes) {

    public NormalizedReading {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId must not be blank");
        }
        if (deviceType == null || deviceType.isBlank()) {
            throw new IllegalArgumentException("deviceType must not be blank");
        }
        if (eventTime == null) {
            throw new IllegalArgumentException("eventTime must not be null");
        }
        if (ingestTime == null) {
            throw new IllegalArgumentException("ingestTime must not be null");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
