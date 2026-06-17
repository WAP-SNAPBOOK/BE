package com.example.easybooking.reservation.dto;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class ReservationCreateRequest {
    private Long shopId;
    private Long staffId;
    private LocalDate date;
    private LocalTime time;
    private String requirements;
    private List<String> imageUrls;
    @Valid
    private List<MenuSelectionRequest> menuSelections;
}
