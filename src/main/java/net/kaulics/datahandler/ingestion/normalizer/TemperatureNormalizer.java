package net.kaulics.datahandler.ingestion.normalizer;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import net.kaulics.datahandler.ingestion.payload.TemperaturePayload;
import net.kaulics.datahandler.model.NormalizedReading;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TemperatureNormalizer implements DeviceNormalizer<TemperaturePayload> {

    public static final String DEVICE_TYPE = "temperature";

    private final Clock clock;

    @Autowired
    public TemperatureNormalizer() {
        this(Clock.systemUTC());
    }

    TemperatureNormalizer(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String deviceType() {
        return DEVICE_TYPE;
    }

    @Override
    public Class<TemperaturePayload> payloadType() {
        return TemperaturePayload.class;
    }

    @Override
    public List<NormalizedReading> normalize(TemperaturePayload payload) {
        NormalizedReading reading = new NormalizedReading(
                payload.deviceId(),
                DEVICE_TYPE,
                payload.timestamp(),
                clock.instant(),
                payload.celsius(),
                "celsius",
                false,
                Map.of());
        return List.of(reading);
    }
}
