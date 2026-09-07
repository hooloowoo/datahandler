package net.kaulics.datahandler.pipeline;

import java.util.List;
import net.kaulics.datahandler.model.NormalizedReading;
import net.kaulics.datahandler.pipeline.OrderingTracker.Classification;
import org.springframework.stereotype.Component;

@Component
public class ReadingPipeline {

    private final OrderingTracker orderingTracker;
    private final ReadingStore store;
    private final List<DownstreamConsumer> consumers;
    private final ReadingMessageQueue queue;

    public ReadingPipeline(
            OrderingTracker orderingTracker,
            ReadingStore store,
            List<DownstreamConsumer> consumers,
            ReadingMessageQueue queue) {
        this.orderingTracker = orderingTracker;
        this.store = store;
        this.consumers = consumers;
        this.queue = queue;
        queue.subscribe(this::process);
    }

    public void accept(NormalizedReading reading) {
        queue.publish(reading);
    }

    private void process(NormalizedReading reading) {
        Classification classification = orderingTracker.classify(reading.deviceId(), reading.eventTime());
        if (classification == Classification.DROPPED) {
            consumers.forEach(c -> c.onDropped(reading.deviceId(), reading.eventTime()));
            return;
        }
        NormalizedReading finalReading =
                classification == Classification.LATE && !reading.late() ? markLate(reading) : reading;
        store.add(finalReading);
        consumers.forEach(c -> c.onReading(finalReading));
    }

    private static NormalizedReading markLate(NormalizedReading reading) {
        return new NormalizedReading(
                reading.deviceId(),
                reading.deviceType(),
                reading.eventTime(),
                reading.ingestTime(),
                reading.value(),
                reading.unit(),
                true,
                reading.attributes());
    }
}
