package net.kaulics.datahandler.ingestion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import net.kaulics.datahandler.ingestion.normalizer.NormalizerRegistry;
import net.kaulics.datahandler.ingestion.normalizer.TemperatureNormalizer;
import net.kaulics.datahandler.ingestion.normalizer.VibrationNormalizer;
import net.kaulics.datahandler.exception.UnknownDeviceTypeException;
import net.kaulics.datahandler.pipeline.ReadingPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class IngestionServiceTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private ReadingPipeline pipeline;
    private IngestionService service;

    @BeforeEach
    void setUp() {
        pipeline = mock(ReadingPipeline.class);
        NormalizerRegistry registry =
                new NormalizerRegistry(List.of(new TemperatureNormalizer(), new VibrationNormalizer()));
        service = new IngestionService(registry, objectMapper, validator, pipeline);
    }

    @Test
    void ingestsTemperaturePayloadAndForwardsToPipeline() {
        int count = service.ingest(
                "temperature",
                Map.of("deviceId", "temp-1", "timestamp", "2024-01-01T00:00:00Z", "celsius", 21.5));

        assertEquals(1, count);
        verify(pipeline, times(1)).accept(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ingestsVibrationBurstAndForwardsEachSample() {
        int count = service.ingest(
                "vibration",
                Map.of(
                        "deviceId", "vib-1",
                        "baseTimestamp", "2024-01-01T00:00:00Z",
                        "sampleRateHz", 1000.0,
                        "samples", List.of(0.1, 0.2, 0.3)));

        assertEquals(3, count);
        verify(pipeline, times(3)).accept(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unknownDeviceTypeThrows() {
        assertThatThrownBy(() -> service.ingest("humidity", Map.of()))
                .isInstanceOf(UnknownDeviceTypeException.class);
    }

    @Test
    void blankDeviceIdFailsValidation() {
        Map<String, Object> payload =
                Map.of("deviceId", "", "timestamp", "2024-01-01T00:00:00Z", "celsius", 21.5);

        assertThatThrownBy(() -> service.ingest("temperature", payload))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void malformedTimestampThrowsIllegalArgument() {
        assertThatThrownBy(() -> service.ingest(
                        "temperature",
                        Map.of("deviceId", "temp-1", "timestamp", "not-a-date", "celsius", 21.5)))
                .isInstanceOf(tools.jackson.databind.DatabindException.class);
    }

    @Test
    void emptySamplesFailsValidation() {
        Map<String, Object> payload = Map.of(
                "deviceId", "vib-1",
                "baseTimestamp", "2024-01-01T00:00:00Z",
                "sampleRateHz", 1000.0,
                "samples", List.of());

        assertThatThrownBy(() -> service.ingest("vibration", payload))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
