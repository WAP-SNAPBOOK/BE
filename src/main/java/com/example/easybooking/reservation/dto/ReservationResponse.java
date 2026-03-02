package com.example.easybooking.reservation.dto;

import com.example.easybooking.reservation.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class ReservationResponse {
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private Reservation.Status status;

    // 추가로 필요한 정보
    private String customerName;
    private Integer photoCount;

    private List<String> photoUrls;
    private String requests;


    public ReservationResponse(
            Reservation entity,
            String customerName,
            Integer photoCount,
            List<String> photoUrls,
            String requests
    ) {
        this.id = entity.getId();
        this.date = entity.getDate();
        this.time = entity.getTime();
        this.status = entity.getStatus();

        this.customerName = customerName;
        this.photoCount = photoCount;

        this.requests = requests;
        this.photoUrls = photoUrls;
    }
}
