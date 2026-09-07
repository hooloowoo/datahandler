package net.kaulics.datahandler.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import net.kaulics.datahandler.pipeline.OrderingTracker.Classification;
import org.junit.jupiter.api.Test;

class OrderingTrackerTest {

    private final OrderingTracker tracker = new OrderingTracker(Duration.ofSeconds(30));

    @Test
    void firstReadingForDeviceIsOnTime() {
        assertEquals(Classification.ON_TIME, tracker.classify("d1", Instant.parse("2024-01-01T00:00:00Z")));
    }

    @Test
    void laterReadingAdvancesWatermarkAndIsOnTime() {
        tracker.classify("d1", Instant.parse("2024-01-01T00:00:00Z"));
        assertEquals(Classification.ON_TIME, tracker.classify("d1", Instant.parse("2024-01-01T00:00:10Z")));
        assertEquals(Instant.parse("2024-01-01T00:00:10Z"), tracker.watermarkFor("d1"));
    }

    @Test
    void slightlyOlderReadingWithinAllowedLatenessIsLate() {
        tracker.classify("d1", Instant.parse("2024-01-01T00:01:00Z"));
        Classification result = tracker.classify("d1", Instant.parse("2024-01-01T00:00:45Z"));
        assertEquals(Classification.LATE, result);
    }

    @Test
    void lateReadingDoesNotMoveWatermarkBackwards() {
        tracker.classify("d1", Instant.parse("2024-01-01T00:01:00Z"));
        tracker.classify("d1", Instant.parse("2024-01-01T00:00:45Z"));
        assertEquals(Instant.parse("2024-01-01T00:01:00Z"), tracker.watermarkFor("d1"));
    }

    @Test
    void readingOlderThanAllowedLatenessIsDropped() {
        tracker.classify("d1", Instant.parse("2024-01-01T00:01:00Z"));
        Classification result = tracker.classify("d1", Instant.parse("2024-01-01T00:00:00Z"));
        assertEquals(Classification.DROPPED, result);
    }

    @Test
    void readingExactlyAtWatermarkIsOnTime() {
        Instant t = Instant.parse("2024-01-01T00:01:00Z");
        tracker.classify("d1", t);
        assertEquals(Classification.ON_TIME, tracker.classify("d1", t));
    }

    @Test
    void devicesAreTrackedIndependently() {
        tracker.classify("d1", Instant.parse("2024-01-01T00:01:00Z"));
        assertEquals(Classification.ON_TIME, tracker.classify("d2", Instant.parse("2020-01-01T00:00:00Z")));
    }
}
