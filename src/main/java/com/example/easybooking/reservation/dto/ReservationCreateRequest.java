package com.example.easybooking.reservation.dto;

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
    private List<MenuSelectionRequest> menuSelections;
}
