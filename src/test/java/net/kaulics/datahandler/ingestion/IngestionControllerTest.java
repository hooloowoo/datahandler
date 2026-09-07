package net.kaulics.datahandler.ingestion;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ingestsTemperatureReading() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/temperature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"deviceId":"temp-1","timestamp":"2024-01-01T00:00:00Z","celsius":21.5}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.deviceType").value("temperature"))
                .andExpect(jsonPath("$.readingsAccepted").value(1));
    }

    @Test
    void ingestsVibrationBurstAndExpandsSamples() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/vibration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"deviceId":"vib-1","baseTimestamp":"2024-01-01T00:00:00Z",
                                 "sampleRateHz":1000.0,"samples":[0.1,0.2,0.3,0.4]}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.readingsAccepted").value(4));
    }

    @Test
    void unknownDeviceTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/humidity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("Unknown device type")));
    }

    @Test
    void invalidPayloadReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/temperature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"deviceId":"","timestamp":"2024-01-01T00:00:00Z","celsius":21.5}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/temperature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest());
    }
}
