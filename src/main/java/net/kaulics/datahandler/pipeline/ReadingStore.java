package net.kaulics.datahandler.pipeline;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.kaulics.datahandler.model.NormalizedReading;
import org.springframework.stereotype.Component;

@Component
public class ReadingStore {

    private final Map<String, ConcurrentSkipListMap<Instant, List<NormalizedReading>>> readingsByDevice =
            new ConcurrentHashMap<>();

    public void add(NormalizedReading reading) {
        readingsByDevice
                .computeIfAbsent(reading.deviceId(), id -> new ConcurrentSkipListMap<>())
                .computeIfAbsent(reading.eventTime(), t -> new CopyOnWriteArrayList<>())
                .add(reading);
    }

    public List<NormalizedReading> readings(String deviceId) {
        var byTime = readingsByDevice.get(deviceId);
        if (byTime == null) {
            return List.of();
        }
        return byTime.values().stream().flatMap(List::stream).toList();
    }

    public List<NormalizedReading> readings(String deviceId, int limit) {
        List<NormalizedReading> all = readings(deviceId);
        if (all.size() <= limit) {
            return all;
        }
        return all.subList(all.size() - limit, all.size());
    }
}
