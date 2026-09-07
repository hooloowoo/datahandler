package net.kaulics.datahandler.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NormalizedReadingTest {

    @Test
    void rejectsBlankDeviceId() {
        Instant now = Instant.now();

        assertThatThrownBy(() -> new NormalizedReading(" ", "temperature", now, now, 1.0, "celsius", false, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankDeviceType() {
        Instant now = Instant.now();

        assertThatThrownBy(() -> new NormalizedReading("temp-1", "", now, now, 1.0, "celsius", false, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullEventTime() {
        Instant now = Instant.now();

        assertThatThrownBy(
                        () -> new NormalizedReading("temp-1", "temperature", null, now, 1.0, "celsius", false, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullIngestTime() {
        Instant now = Instant.now();

        assertThatThrownBy(
                        () -> new NormalizedReading("temp-1", "temperature", now, null, 1.0, "celsius", false, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defaultsNullAttributesToEmptyMap() {
        NormalizedReading reading = new NormalizedReading(
                "temp-1", "temperature", Instant.now(), Instant.now(), 1.0, "celsius", false, null);
        assertTrue(reading.attributes().isEmpty());
    }

    @Test
    void attributesAreImmutableCopy() {
        Map<String, Object> mutable = new java.util.HashMap<>();
        mutable.put("k", "v");
        NormalizedReading reading = new NormalizedReading(
                "temp-1", "temperature", Instant.now(), Instant.now(), 1.0, "celsius", false, mutable);
        mutable.put("k2", "v2");
        assertEquals(1, reading.attributes().size());
        assertFalse(reading.attributes().containsKey("k2"));
    }
}
