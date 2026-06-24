package com.example.easybooking.reservation.dto;

import com.example.easybooking.reservation.domain.Reservation;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ReservationStatusResponse {
    private Reservation.Status status;   // CONFIRMED 또는 REJECTED
    private String customerName;
    private LocalDate date;
    private LocalTime time;

    // 확정 시: 점주 입력 메시지
    private String message;

    // 거절 시: 점주 입력 거절 사유
    private String rejectReason;

    private Reservation.CanceledByType canceledByType;
    private Long canceledByUserId;
    private LocalDateTime canceledAt;
    private String cancelReason;
    private Reservation.CancelTiming cancelTiming;
    private Boolean refundEligible;
}
