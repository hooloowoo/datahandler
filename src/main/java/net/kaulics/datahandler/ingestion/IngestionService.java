package net.kaulics.datahandler.ingestion;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kaulics.datahandler.ingestion.normalizer.DeviceNormalizer;
import net.kaulics.datahandler.ingestion.normalizer.NormalizerRegistry;
import net.kaulics.datahandler.exception.UnknownDeviceTypeException;
import net.kaulics.datahandler.model.NormalizedReading;
import net.kaulics.datahandler.pipeline.ReadingPipeline;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class IngestionService {

    private final NormalizerRegistry registry;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final ReadingPipeline pipeline;

    public IngestionService(
            NormalizerRegistry registry, ObjectMapper objectMapper, Validator validator, ReadingPipeline pipeline) {
        this.registry = registry;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.pipeline = pipeline;
    }

    public int ingest(String deviceType, Map<String, Object> rawPayload) {
        DeviceNormalizer<?> normalizer =
                registry.find(deviceType).orElseThrow(() -> new UnknownDeviceTypeException(deviceType));
        return ingestTyped(normalizer, rawPayload);
    }

    private <T> int ingestTyped(DeviceNormalizer<T> normalizer, Map<String, Object> rawPayload) {
        T payload = objectMapper.convertValue(rawPayload, normalizer.payloadType());
        validate(payload);
        List<NormalizedReading> readings = normalizer.normalize(payload);
        readings.forEach(pipeline::accept);
        return readings.size();
    }

    private <T> void validate(T payload) {
        Set<ConstraintViolation<T>> violations = validator.validate(payload);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
