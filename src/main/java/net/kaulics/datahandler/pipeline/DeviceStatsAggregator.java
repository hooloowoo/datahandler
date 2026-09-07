package net.kaulics.datahandler.pipeline;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kaulics.datahandler.model.DeviceStats;
import net.kaulics.datahandler.model.NormalizedReading;
import org.springframework.stereotype.Component;

@Component
public class DeviceStatsAggregator implements DownstreamConsumer {

    private final Map<String, Accumulator> accumulators = new ConcurrentHashMap<>();

    @Override
    public void onReading(NormalizedReading reading) {
        accumulators
                .computeIfAbsent(reading.deviceId(), id -> new Accumulator())
                .add(reading.value(), reading.late());
    }

    @Override
    public void onDropped(String deviceId, Instant eventTime) {
        accumulators.computeIfAbsent(deviceId, id -> new Accumulator()).drop();
    }

    public DeviceStats snapshot(String deviceId) {
        Accumulator acc = accumulators.get(deviceId);
        return acc == null ? DeviceStats.empty(deviceId) : acc.snapshot(deviceId);
    }

    private static final class Accumulator {
        private long count;
        private long lateCount;
        private long droppedCount;
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;
        private double sum;

        synchronized void add(double value, boolean late) {
            count++;
            if (late) {
                lateCount++;
            }
            min = Math.min(min, value);
            max = Math.max(max, value);
            sum += value;
        }

        synchronized void drop() {
            droppedCount++;
        }

        synchronized DeviceStats snapshot(String deviceId) {
            double average = count == 0 ? Double.NaN : sum / count;
            return new DeviceStats(deviceId, count, lateCount, droppedCount, min, max, average);
        }
    }
}
