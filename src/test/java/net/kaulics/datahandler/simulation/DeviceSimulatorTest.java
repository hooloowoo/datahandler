package net.kaulics.datahandler.simulation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;
import net.kaulics.datahandler.ingestion.IngestionService;
import org.junit.jupiter.api.Test;

class DeviceSimulatorTest {

    private final IngestionService ingestionService = mock(IngestionService.class);
    private final DeviceSimulator simulator = new DeviceSimulator(ingestionService);

    @Test
    void emitsTemperatureReadingThroughIngestionService() {
        simulator.emitTemperatureReading();

        verify(ingestionService).ingest(eq("temperature"), any());
    }

    @Test
    void emitsVibrationBurstThroughIngestionService() {
        simulator.emitVibrationBurst();

        verify(ingestionService).ingest(eq("vibration"), any());
    }

    @Test
    void temperaturePayloadHasExpectedFields() {
        simulator.emitTemperatureReading();

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(ingestionService).ingest(eq("temperature"), captor.capture());
        Map<String, Object> payload = captor.getValue();

        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("deviceId"));
        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("timestamp"));
        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("celsius"));
    }

    @Test
    void vibrationPayloadHasExpectedFields() {
        simulator.emitVibrationBurst();

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(ingestionService).ingest(eq("vibration"), captor.capture());
        Map<String, Object> payload = captor.getValue();

        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("deviceId"));
        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("baseTimestamp"));
        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("sampleRateHz"));
        org.junit.jupiter.api.Assertions.assertTrue(payload.containsKey("samples"));
    }
}
