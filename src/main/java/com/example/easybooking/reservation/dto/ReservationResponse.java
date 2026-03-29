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
    private Integer imageCount;

    private List<String> photoUrls;
    private List<String> imageUrls;
    private String requests;
    private String requirements;


    public ReservationResponse(
            Reservation entity,
            String customerName
    ) {
        List<String> imageUrls = entity.getDesignImageURLs() == null
                ? List.of()
                : List.copyOf(entity.getDesignImageURLs());
        int imageCount = imageUrls.size();

        this.id = entity.getId();
        this.date = entity.getDate();
        this.time = entity.getTime();
        this.status = entity.getStatus();

        this.customerName = customerName;
        this.photoCount = imageCount;
        this.imageCount = imageCount;

        this.requests = entity.getRequirements();
        this.requirements = entity.getRequirements();
        this.photoUrls = imageUrls;
        this.imageUrls = imageUrls;
    }
}
