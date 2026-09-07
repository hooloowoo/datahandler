package net.kaulics.datahandler.delegate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ReadingQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void readingsAndStatsAreAvailableAfterIngestion() throws Exception {
        mockMvc.perform(post("/api/v1/ingest/temperature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"deviceId":"temp-query-test","timestamp":"2024-01-01T00:00:00Z","celsius":25.0}
                                """))
                .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/v1/devices/temp-query-test/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("temp-query-test"))
                .andExpect(jsonPath("$[0].value").value(25.0));

        mockMvc.perform(get("/api/v1/devices/temp-query-test/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.average").value(25.0));
    }

    @Test
    void readingsForUnknownDeviceIsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/devices/never-seen/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void statsForUnknownDeviceIsEmptySnapshot() throws Exception {
        mockMvc.perform(get("/api/v1/devices/never-seen-stats/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }
}
