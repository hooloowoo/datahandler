package net.kaulics.datahandler.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import net.kaulics.datahandler.model.NormalizedReading;
import org.junit.jupiter.api.Test;

class ReadingPipelineTest {

    private final OrderingTracker orderingTracker = new OrderingTracker(Duration.ofSeconds(30));
    private final ReadingStore store = new ReadingStore();
    private final DeviceStatsAggregator statsAggregator = new DeviceStatsAggregator();
    private final ReadingPipeline pipeline = new ReadingPipeline(
            orderingTracker, store, List.of(statsAggregator), new ReadingMessageQueue(null, null, false));

    private static NormalizedReading reading(Instant eventTime, double value) {
        return new NormalizedReading("d1", "temperature", eventTime, Instant.now(), value, "celsius", false,
                Map.of());
    }

    @Test
    void onTimeReadingIsStoredAndForwarded() {
        pipeline.accept(reading(Instant.parse("2024-01-01T00:00:00Z"), 21));

        assertEquals(1, store.readings("d1").size());
        assertFalse(store.readings("d1").get(0).late());
        assertEquals(1, statsAggregator.snapshot("d1").count());
    }

    @Test
    void lateReadingIsStoredAndFlagged() {
        pipeline.accept(reading(Instant.parse("2024-01-01T00:01:00Z"), 21));
        pipeline.accept(reading(Instant.parse("2024-01-01T00:00:45Z"), 22));

        List<NormalizedReading> readings = store.readings("d1");
        assertEquals(2, readings.size());
        assertTrue(readings.get(0).late());
        assertEquals(1, statsAggregator.snapshot("d1").lateCount());
    }

    @Test
    void tooLateReadingIsDroppedNotStored() {
        pipeline.accept(reading(Instant.parse("2024-01-01T00:01:00Z"), 21));
        pipeline.accept(reading(Instant.parse("2024-01-01T00:00:00Z"), 22));

        assertEquals(1, store.readings("d1").size());
        assertEquals(1, statsAggregator.snapshot("d1").droppedCount());
    }
}
