package net.kaulics.datahandler.pipeline;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderingTracker {

    public enum Classification { ON_TIME, LATE, DROPPED }

    private final Duration allowedLateness;
    private final Map<String, Instant> watermarks = new ConcurrentHashMap<>();

    public OrderingTracker(@Value("${ingestion.allowed-lateness:PT30S}") Duration allowedLateness) {
        this.allowedLateness = allowedLateness;
    }

    public synchronized Classification classify(String deviceId, Instant eventTime) {
        Instant watermark = watermarks.putIfAbsent(deviceId, eventTime);
        if (watermark == null) {
            return Classification.ON_TIME;
        }
        if (eventTime.isBefore(watermark.minus(allowedLateness))) {
            return Classification.DROPPED;
        }
        if (eventTime.isBefore(watermark)) {
            return Classification.LATE;
        }
        watermarks.put(deviceId, eventTime);
        return Classification.ON_TIME;
    }

    public Instant watermarkFor(String deviceId) {
        return watermarks.getOrDefault(deviceId, Instant.MIN);
    }
}
