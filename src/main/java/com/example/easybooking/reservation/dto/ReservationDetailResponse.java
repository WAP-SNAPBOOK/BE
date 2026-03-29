package com.example.easybooking.reservation.dto;

import com.example.easybooking.reservation.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationDetailResponse {
    private Long id;
    private Reservation.Status status;
    private LocalDate date;
    private LocalTime time;
    private LocalDateTime createdAt;

    private Long shopId;
    private String shopName;

    private String customerName;
    private String customerPhone;

    private String rejectionReason;
    private String confirmationMessage;

    private List<String> photoUrls;
    private int photoCount;

    private String part;
    private String removal;
    private String requests;

    private Integer extendCount;
    private Integer wrappingCount;

    private String extendStatus;
    private String wrappingStatus;

    private List<ReservationMenuItemResponse> menus;
}

