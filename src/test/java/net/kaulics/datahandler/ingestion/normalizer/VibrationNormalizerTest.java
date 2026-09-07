package net.kaulics.datahandler.ingestion.normalizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import net.kaulics.datahandler.ingestion.payload.VibrationPayload;
import net.kaulics.datahandler.model.NormalizedReading;
import org.junit.jupiter.api.Test;

class VibrationNormalizerTest {

    private final Instant fixedNow = Instant.parse("2024-01-01T00:00:10Z");
    private final VibrationNormalizer normalizer = new VibrationNormalizer(Clock.fixed(fixedNow, ZoneOffset.UTC));

    @Test
    void deviceTypeIsVibration() {
        assertEquals("vibration", normalizer.deviceType());
    }

    @Test
    void payloadTypeIsVibrationPayload() {
        assertEquals(VibrationPayload.class, normalizer.payloadType());
    }

    @Test
    void expandsBurstIntoOneReadingPerSample() {
        Instant baseTimestamp = Instant.parse("2024-01-01T00:00:00Z");
        VibrationPayload payload = new VibrationPayload("vib-1", baseTimestamp, 1000.0, List.of(0.1, 0.2, 0.3));

        List<NormalizedReading> readings = normalizer.normalize(payload);

        assertEquals(3, readings.size());
        for (int i = 0; i < readings.size(); i++) {
            NormalizedReading reading = readings.get(i);
            assertEquals("vib-1", reading.deviceId());
            assertEquals("vibration", reading.deviceType());
            assertEquals(baseTimestamp.plusNanos(i * 1_000_000L), reading.eventTime());
            assertEquals(fixedNow, reading.ingestTime());
            assertEquals(payload.samples().get(i), reading.value());
            assertEquals("g", reading.unit());
            assertFalse(reading.late());
            assertEquals(i, reading.attributes().get("sampleIndex"));
        }
    }

    @Test
    void singleSampleBurstProducesOneReadingAtBaseTimestamp() {
        Instant baseTimestamp = Instant.parse("2024-01-01T00:00:00Z");
        VibrationPayload payload = new VibrationPayload("vib-1", baseTimestamp, 500.0, List.of(0.5));

        List<NormalizedReading> readings = normalizer.normalize(payload);

        assertEquals(1, readings.size());
        assertEquals(baseTimestamp, readings.get(0).eventTime());
    }
}
