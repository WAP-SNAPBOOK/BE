package com.example.easybooking.reservation.dto;

import com.example.easybooking.reservation.domain.Reservation;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationResponse {
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private Reservation.Status status;

    // 추가로 필요한 정보 ?
    // 예약 시각, 샵 이름 등

    public ReservationResponse(Reservation entity) {
        this.id = entity.getId();
        this.date = entity.getDate();
        this.time = entity.getTime();
        this.status = entity.getStatus();
    }
}
