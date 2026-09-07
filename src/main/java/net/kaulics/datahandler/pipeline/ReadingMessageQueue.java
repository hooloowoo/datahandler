package net.kaulics.datahandler.pipeline;

import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import net.kaulics.datahandler.model.NormalizedReading;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class ReadingMessageQueue {

    static final String DESTINATION = "readings";

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final boolean asyncEnabled;
    private final AtomicReference<Consumer<NormalizedReading>> handler = new AtomicReference<>(reading -> {});

    public ReadingMessageQueue(
            JmsTemplate jmsTemplate,
            ObjectMapper objectMapper,
            @Value("${pipeline.async.enabled:true}") boolean asyncEnabled) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.asyncEnabled = asyncEnabled;
    }

    void subscribe(Consumer<NormalizedReading> handler) {
        this.handler.set(handler);
    }

    public void publish(NormalizedReading reading) {
        if (asyncEnabled) {
            jmsTemplate.convertAndSend(DESTINATION, objectMapper.writeValueAsString(reading));
        } else {
            handler.get().accept(reading);
        }
    }

    @JmsListener(destination = DESTINATION)
    void onMessage(String json) {
        handler.get().accept(objectMapper.readValue(json, NormalizedReading.class));
    }
}

