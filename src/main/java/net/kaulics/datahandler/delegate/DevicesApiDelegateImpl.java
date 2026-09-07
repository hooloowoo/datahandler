package net.kaulics.datahandler.delegate;

import java.time.ZoneOffset;
import java.util.List;
import net.kaulics.datahandler.api.DevicesApiDelegate;
import net.kaulics.datahandler.api.model.DeviceStats;
import net.kaulics.datahandler.api.model.Reading;
import net.kaulics.datahandler.pipeline.DeviceStatsAggregator;
import net.kaulics.datahandler.pipeline.ReadingStore;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DevicesApiDelegateImpl implements DevicesApiDelegate {

    private final ReadingStore store;
    private final DeviceStatsAggregator statsAggregator;

    public DevicesApiDelegateImpl(ReadingStore store, DeviceStatsAggregator statsAggregator) {
        this.store = store;
        this.statsAggregator = statsAggregator;
    }

    @Override
    public ResponseEntity<List<Reading>> getDeviceReadings(String deviceId, Integer limit) {
        int effectiveLimit = limit == null ? 100 : limit;
        List<Reading> readings =
                store.readings(deviceId, effectiveLimit).stream().map(this::toApiReading).toList();
        return ResponseEntity.ok(readings);
    }

    @Override
    public ResponseEntity<DeviceStats> getDeviceStats(String deviceId) {
        return ResponseEntity.ok(toApiStats(statsAggregator.snapshot(deviceId)));
    }

    private Reading toApiReading(net.kaulics.datahandler.model.NormalizedReading reading) {
        return new Reading(
                        reading.deviceId(),
                        reading.deviceType(),
                        reading.eventTime().atOffset(ZoneOffset.UTC),
                        reading.ingestTime().atOffset(ZoneOffset.UTC),
                        reading.value(),
                        reading.unit(),
                        reading.late())
                .attributes(reading.attributes());
    }

    private DeviceStats toApiStats(net.kaulics.datahandler.model.DeviceStats stats) {
        return new DeviceStats(
                stats.deviceId(), stats.count(), stats.lateCount(), stats.droppedCount(),
                stats.min(), stats.max(), stats.average());
    }
}
