package com.aeroops.network.gate;

import com.aeroops.common.error.ConflictException;
import com.aeroops.common.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GateController.class)
class GateControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    GateService service;

    private static final String VALID_BODY = """
            {"code":"A1","type":"CONTACT","sizeCategory":"C"}
            """;

    @Test
    void create_returns201WithNestedLocation() throws Exception {
        when(service.create(eq(1L), any())).thenReturn(
                new GateResponse(7L, 1L, "TIA", "A1", GateType.CONTACT, SizeCategory.C));

        mvc.perform(post("/api/airports/1/gates").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/airports/1/gates/7"))
                .andExpect(jsonPath("$.airportIata").value("TIA"));
    }

    @Test
    void create_returns400ForUnknownEnumValue() throws Exception {
        mvc.perform(post("/api/airports/1/gates").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"A1\",\"type\":\"BRIDGE\",\"sizeCategory\":\"C\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request"));
    }

    @Test
    void create_returns400ForMissingFields() throws Exception {
        mvc.perform(post("/api/airports/1/gates").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"A-1!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").exists())
                .andExpect(jsonPath("$.errors.type").exists())
                .andExpect(jsonPath("$.errors.sizeCategory").exists());
    }

    @Test
    void create_returns409OnDuplicate() throws Exception {
        when(service.create(eq(1L), any())).thenThrow(new ConflictException("Gate A1 already exists at TIA"));

        mvc.perform(post("/api/airports/1/gates").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void get_returns404ForUnknownGate() throws Exception {
        when(service.findById(1L, 99L)).thenThrow(new NotFoundException("Gate", 99L));

        mvc.perform(get("/api/airports/1/gates/99"))
                .andExpect(status().isNotFound());
    }
}
