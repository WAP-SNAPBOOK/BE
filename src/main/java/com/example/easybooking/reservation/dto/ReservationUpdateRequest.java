package com.example.easybooking.reservation.dto;

import com.example.easybooking.common.validation.MultipleOf;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class ReservationUpdateRequest {

    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startAt;

    @Min(value = 30, message = "예약 시간은 30분 이상이어야 합니다.")
    @Max(value = 180, message = "예약 시간은 180분 이하여야 합니다.")
    @MultipleOf(base = 30, message = "예약 시간은 30분 단위여야 합니다.")
    private Integer durationMinutes;

    private Long staffId;

    private List<MenuSelectionRequest> menuSelections;

    private String message;
}
