package com.example.easybooking.reservation.dto;

import com.example.easybooking.reservation.domain.Reservation;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ReservationResponse {
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private Reservation.Status status;

    // 추가로 필요한 정보
    private String customerName;
    private Integer photoCount;

    private String part;
    private String removal;
    private Integer extendCount;
    private Integer wrappingCount;
    private String extendStatus;     // 연장 유/무 (화면 표시용)
    private String wrappingStatus;   // 래핑 유/무 (화면 표시용)
    private List<String> photoUrls;
    private String requests;


    public ReservationResponse(
            Reservation entity,
            String customerName,
            Integer photoCount,
            String part,
            String removal,
            Integer extendCount,
            Integer wrappingCount,
            List<String> photoUrls,
            String requests
    ) {
        this.id = entity.getId();
        this.date = entity.getDate();
        this.time = entity.getTime();
        this.status = entity.getStatus();

        this.customerName = customerName;
        this.photoCount = photoCount;

        this.part = part;
        this.removal = removal;
        this.requests = requests;
        this.photoUrls = photoUrls;

        this.extendCount = extendCount;
        this.wrappingCount = wrappingCount;

        // 수량을 기반으로 '유/무' 상태로 바꾸기
        this.extendStatus = (extendCount != null && extendCount > 0) ? "유" : "무";
        this.wrappingStatus = (wrappingCount != null && wrappingCount > 0) ? "유" : "무";
    }
}
