package net.kaulics.datahandler.ingestion.normalizer;

import net.kaulics.datahandler.model.NormalizedReading;
import java.util.List;

public interface DeviceNormalizer<T> {

    String deviceType();

    Class<T> payloadType();

    List<NormalizedReading> normalize(T payload);
}
