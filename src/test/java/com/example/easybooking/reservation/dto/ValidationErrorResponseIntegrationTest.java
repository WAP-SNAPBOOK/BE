package com.example.easybooking.reservation.dto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(ValidationErrorResponseIntegrationTest.TestController.class)
class ValidationErrorResponseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @RestController
    static class TestController {
        @PostMapping("/__test/validation")
        void test(@Valid @RequestBody ReservationConfirmRequest req) {
        }
    }

    @Test
    void whenInvalid_thenReturns400_andShowsBodyShape() throws Exception {
        mockMvc.perform(post("/__test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" adsfsafd \",\"durationMinutes\":25}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[0]").value(org.hamcrest.Matchers.containsString("durationMinutes")));
    }
}