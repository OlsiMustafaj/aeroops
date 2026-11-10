package com.aeroops.network.airport;

import com.aeroops.common.error.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
class AirportControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AirportService service;

    private static final String VALID_BODY = """
            {"iataCode":"TIA","icaoCode":"LATA","name":"Tirana International",
             "city":"Tirana","country":"Albania","timezone":"Europe/Tirane"}
            """;

    @Test
    void create_returns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(new AirportResponse(1L, "TIA", "LATA",
                "Tirana International", "Tirana", "Albania", "Europe/Tirane"));

        mvc.perform(post("/api/airports").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/airports/1"))
                .andExpect(jsonPath("$.iataCode").value("TIA"));
    }

    @Test
    void create_returns400ForInvalidCodes() throws Exception {
        String body = """
                {"iataCode":"TI","icaoCode":"LATA1","name":"","city":"Tirana",
                 "country":"Albania","timezone":"Europe/Tirane"}
                """;

        mvc.perform(post("/api/airports").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.iataCode").exists())
                .andExpect(jsonPath("$.errors.icaoCode").exists())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void create_returns409OnDuplicate() throws Exception {
        when(service.create(any())).thenThrow(new ConflictException("Airport with IATA code TIA already exists"));

        mvc.perform(post("/api/airports").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Airport with IATA code TIA already exists"));
    }
}
