package net.kaulics.datahandler.ingestion;

import java.util.Map;
import net.kaulics.datahandler.api.IngestionApiDelegate;
import net.kaulics.datahandler.api.model.IngestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class IngestionApiDelegateImpl implements IngestionApiDelegate {

    private final IngestionService ingestionService;

    public IngestionApiDelegateImpl(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public ResponseEntity<IngestResponse> ingestDeviceData(String deviceType, Map<String, Object> requestBody) {
        int accepted = ingestionService.ingest(deviceType, requestBody);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new IngestResponse(deviceType, accepted));
    }
}
