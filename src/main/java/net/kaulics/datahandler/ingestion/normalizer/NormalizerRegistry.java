package net.kaulics.datahandler.ingestion.normalizer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class NormalizerRegistry {

    private final Map<String, DeviceNormalizer<?>> normalizersByType;

    public NormalizerRegistry(List<DeviceNormalizer<?>> normalizers) {
        this.normalizersByType = normalizers.stream()
                .collect(Collectors.toUnmodifiableMap(DeviceNormalizer::deviceType, n -> n));
    }

    public Optional<DeviceNormalizer<?>> find(String deviceType) {
        return Optional.ofNullable(normalizersByType.get(deviceType));
    }

    public boolean supports(String deviceType) {
        return normalizersByType.containsKey(deviceType);
    }
}
