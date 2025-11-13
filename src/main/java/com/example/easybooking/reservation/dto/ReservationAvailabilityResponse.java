package com.example.easybooking.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ReservationAvailabilityResponse {
    private LocalDate date;
    private List<LocalTime> bookedTimes;   // 예약이 확정된 시간 목록 (예약 불가 시간)
}
