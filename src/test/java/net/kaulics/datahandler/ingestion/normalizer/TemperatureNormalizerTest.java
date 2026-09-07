package net.kaulics.datahandler.ingestion.normalizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import net.kaulics.datahandler.ingestion.payload.TemperaturePayload;
import net.kaulics.datahandler.model.NormalizedReading;
import org.junit.jupiter.api.Test;

class TemperatureNormalizerTest {

    private final Instant fixedNow = Instant.parse("2024-01-01T00:00:10Z");
    private final TemperatureNormalizer normalizer =
            new TemperatureNormalizer(Clock.fixed(fixedNow, ZoneOffset.UTC));

    @Test
    void deviceTypeIsTemperature() {
        assertEquals("temperature", normalizer.deviceType());
    }

    @Test
    void payloadTypeIsTemperaturePayload() {
        assertEquals(TemperaturePayload.class, normalizer.payloadType());
    }

    @Test
    void normalizesSingleReading() {
        Instant eventTime = Instant.parse("2024-01-01T00:00:00Z");
        TemperaturePayload payload = new TemperaturePayload("temp-1", eventTime, 21.5);

        List<NormalizedReading> readings = normalizer.normalize(payload);

        assertEquals(1, readings.size());
        NormalizedReading reading = readings.get(0);
        assertEquals("temp-1", reading.deviceId());
        assertEquals("temperature", reading.deviceType());
        assertEquals(eventTime, reading.eventTime());
        assertEquals(fixedNow, reading.ingestTime());
        assertEquals(21.5, reading.value());
        assertEquals("celsius", reading.unit());
        assertFalse(reading.late());
        assertNotNull(reading.attributes());
    }
}
