package net.kaulics.datahandler.ingestion.normalizer;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kaulics.datahandler.ingestion.payload.VibrationPayload;
import net.kaulics.datahandler.model.NormalizedReading;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VibrationNormalizer implements DeviceNormalizer<VibrationPayload> {

    public static final String DEVICE_TYPE = "vibration";

    private final Clock clock;

    @Autowired
    public VibrationNormalizer() {
        this(Clock.systemUTC());
    }

    VibrationNormalizer(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String deviceType() {
        return DEVICE_TYPE;
    }

    @Override
    public Class<VibrationPayload> payloadType() {
        return VibrationPayload.class;
    }

    @Override
    public List<NormalizedReading> normalize(VibrationPayload payload) {
        var ingestTime = clock.instant();
        var sampleInterval = Duration.ofNanos((long) (1_000_000_000L / payload.sampleRateHz()));
        List<Double> samples = payload.samples();
        List<NormalizedReading> readings = new ArrayList<>(samples.size());
        for (int i = 0; i < samples.size(); i++) {
            var eventTime = payload.baseTimestamp().plus(sampleInterval.multipliedBy(i));
            readings.add(new NormalizedReading(
                    payload.deviceId(),
                    DEVICE_TYPE,
                    eventTime,
                    ingestTime,
                    samples.get(i),
                    "g",
                    false,
                    Map.of("sampleIndex", i)));
        }
        return readings;
    }
}
