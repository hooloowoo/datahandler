package net.kaulics.datahandler.simulation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.kaulics.datahandler.ingestion.IngestionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "simulation.enabled", havingValue = "true", matchIfMissing = true)
public class DeviceSimulator {

    private static final String TEMPERATURE_DEVICE_ID = "temp-1";
    private static final String VIBRATION_DEVICE_ID = "vib-1";

    private final IngestionService ingestionService;

    public DeviceSimulator(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Scheduled(fixedRate = 5000)
    public void emitTemperatureReading() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Instant timestamp = maybeLate(Instant.now(), random);
        double celsius = 18 + random.nextDouble(-3, 3);
        ingestionService.ingest(
                "temperature",
                Map.of(
                        "deviceId", TEMPERATURE_DEVICE_ID,
                        "timestamp", timestamp.toString(),
                        "celsius", celsius));
    }

    @Scheduled(fixedRate = 15000)
    public void emitVibrationBurst() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Instant baseTimestamp = maybeLate(Instant.now(), random);
        int sampleCount = 50;
        List<Double> samples = random.doubles(sampleCount, 0.0, 2.0).boxed().toList();
        ingestionService.ingest(
                "vibration",
                Map.of(
                        "deviceId", VIBRATION_DEVICE_ID,
                        "baseTimestamp", baseTimestamp.toString(),
                        "sampleRateHz", 1000.0,
                        "samples", samples));
    }

    private Instant maybeLate(Instant now, ThreadLocalRandom random) {
        if (random.nextInt(10) == 0) {
            return now.minus(Duration.ofSeconds(random.nextInt(5, 20)));
        }
        return now;
    }
}
