package net.kaulics.datahandler.ingestion.normalizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.kaulics.datahandler.ingestion.payload.TemperaturePayload;
import net.kaulics.datahandler.ingestion.payload.VibrationPayload;
import org.junit.jupiter.api.Test;

class NormalizerRegistryTest {

    private final TemperatureNormalizer temperatureNormalizer = new TemperatureNormalizer();
    private final VibrationNormalizer vibrationNormalizer = new VibrationNormalizer();
    private final NormalizerRegistry registry =
            new NormalizerRegistry(List.of(temperatureNormalizer, vibrationNormalizer));

    @Test
    void findsRegisteredNormalizerByType() {
        assertEquals(temperatureNormalizer, registry.find("temperature").orElseThrow());
        assertEquals(vibrationNormalizer, registry.find("vibration").orElseThrow());
    }

    @Test
    void returnsEmptyForUnknownType() {
        assertTrue(registry.find("humidity").isEmpty());
    }

    @Test
    void supportsReflectsRegisteredTypes() {
        assertTrue(registry.supports("temperature"));
        assertFalse(registry.supports("humidity"));
    }

    @Test
    void payloadTypesAreDistinct() {
        assertEquals(TemperaturePayload.class, registry.find("temperature").orElseThrow().payloadType());
        assertEquals(VibrationPayload.class, registry.find("vibration").orElseThrow().payloadType());
    }
}
