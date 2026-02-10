package com.example.easybooking.reservation.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ReservationConfirmRequestValidationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void confirmRequest_deserializesWithDurationMinutes() throws Exception {
        objectMapper.readValue(
                "{\"message\":\"ok\",\"durationMinutes\":60}",
                ReservationConfirmRequest.class
        );
    }
}

