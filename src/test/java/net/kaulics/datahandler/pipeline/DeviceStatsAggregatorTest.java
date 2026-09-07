package net.kaulics.datahandler.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import net.kaulics.datahandler.model.DeviceStats;
import net.kaulics.datahandler.model.NormalizedReading;
import org.junit.jupiter.api.Test;

class DeviceStatsAggregatorTest {

    private final DeviceStatsAggregator aggregator = new DeviceStatsAggregator();

    private static NormalizedReading reading(double value, boolean late) {
        return new NormalizedReading("d1", "temperature", Instant.now(), Instant.now(), value, "celsius", late,
                Map.of());
    }

    @Test
    void snapshotForUnknownDeviceIsEmpty() {
        DeviceStats stats = aggregator.snapshot("unknown");
        assertEquals(0, stats.count());
        assertTrue(Double.isNaN(stats.average()));
    }

    @Test
    void aggregatesMinMaxAverage() {
        aggregator.onReading(reading(10, false));
        aggregator.onReading(reading(20, false));
        aggregator.onReading(reading(30, false));

        DeviceStats stats = aggregator.snapshot("d1");
        assertEquals(3, stats.count());
        assertEquals(10, stats.min());
        assertEquals(30, stats.max());
        assertEquals(20, stats.average());
        assertEquals(0, stats.lateCount());
    }

    @Test
    void countsLateReadings() {
        aggregator.onReading(reading(10, false));
        aggregator.onReading(reading(20, true));

        assertEquals(1, aggregator.snapshot("d1").lateCount());
        assertEquals(2, aggregator.snapshot("d1").count());
    }

    @Test
    void countsDroppedReadings() {
        aggregator.onDropped("d1", Instant.now());
        aggregator.onDropped("d1", Instant.now());

        assertEquals(2, aggregator.snapshot("d1").droppedCount());
        assertEquals(0, aggregator.snapshot("d1").count());
    }

    @Test
    void devicesAreTrackedIndependently() {
        aggregator.onReading(reading(10, false));
        aggregator.onReading(new NormalizedReading("d2", "temperature", Instant.now(), Instant.now(), 100, "celsius",
                false, Map.of()));

        assertEquals(10, aggregator.snapshot("d1").average());
        assertEquals(100, aggregator.snapshot("d2").average());
    }
}
