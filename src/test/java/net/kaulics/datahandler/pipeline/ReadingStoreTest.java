package net.kaulics.datahandler.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import net.kaulics.datahandler.model.NormalizedReading;
import org.junit.jupiter.api.Test;

class ReadingStoreTest {

    private final ReadingStore store = new ReadingStore();

    private static NormalizedReading reading(String deviceId, Instant eventTime, double value) {
        return new NormalizedReading(deviceId, "temperature", eventTime, Instant.now(), value, "celsius", false,
                Map.of());
    }

    @Test
    void returnsEmptyListForUnknownDevice() {
        assertTrue(store.readings("unknown").isEmpty());
    }

    @Test
    void ordersReadingsByEventTimeRegardlessOfInsertionOrder() {
        Instant t1 = Instant.parse("2024-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2024-01-01T00:00:10Z");
        Instant t3 = Instant.parse("2024-01-01T00:00:20Z");

        store.add(reading("d1", t3, 3));
        store.add(reading("d1", t1, 1));
        store.add(reading("d1", t2, 2));

        List<NormalizedReading> readings = store.readings("d1");
        assertEquals(List.of(1.0, 2.0, 3.0), readings.stream().map(NormalizedReading::value).toList());
    }

    @Test
    void keepsMultipleReadingsWithSameEventTime() {
        Instant t = Instant.parse("2024-01-01T00:00:00Z");
        store.add(reading("d1", t, 1));
        store.add(reading("d1", t, 2));

        assertEquals(2, store.readings("d1").size());
    }

    @Test
    void limitReturnsMostRecentReadings() {
        for (int i = 0; i < 5; i++) {
            store.add(reading("d1", Instant.parse("2024-01-01T00:00:0" + i + "Z"), i));
        }

        List<NormalizedReading> limited = store.readings("d1", 2);
        assertEquals(List.of(3.0, 4.0), limited.stream().map(NormalizedReading::value).toList());
    }

    @Test
    void limitLargerThanSizeReturnsAll() {
        store.add(reading("d1", Instant.parse("2024-01-01T00:00:00Z"), 1));
        assertEquals(1, store.readings("d1", 100).size());
    }

    @Test
    void devicesAreIsolated() {
        store.add(reading("d1", Instant.parse("2024-01-01T00:00:00Z"), 1));
        assertTrue(store.readings("d2").isEmpty());
    }
}
