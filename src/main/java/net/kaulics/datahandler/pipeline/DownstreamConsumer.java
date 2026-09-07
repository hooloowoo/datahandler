package net.kaulics.datahandler.pipeline;

import java.time.Instant;
import net.kaulics.datahandler.model.NormalizedReading;

public interface DownstreamConsumer {

    void onReading(NormalizedReading reading);

    default void onDropped(String deviceId, Instant eventTime) {
    }
}
