package com.example.easybooking.reservation.dto;

import com.example.easybooking.reservation.domain.Reservation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ReservationOwnerResponse {
    private Long id;
    private String customerName;
    private String customerPhone;
    private Reservation.Status status;
    private LocalDate date;
    private LocalTime time;
    private int photoCount;           // 첨부 사진 갯수
    private List<String> photoUrls;   // 첨부 사진 URL 목록
    private String rejectionReason;      // 거절 사유 (거절 시)
    private String confirmationMessage;  // 전달 사항 (확정 시)
    private LocalDateTime createdAt;

    public static ReservationOwnerResponse from(
            Reservation reservation,
            String customerName,
            String customerPhone
    ) {
        List<String> photoUrls = reservation.getDesignImageURLs();

        return ReservationOwnerResponse.builder()
                .id(reservation.getId())
                .customerName(customerName)
                .customerPhone(customerPhone)
                .status(reservation.getStatus())
                .date(reservation.getDate())
                .time(reservation.getTime())
                .photoCount(photoUrls.size())
                .photoUrls(photoUrls)
                .rejectionReason(reservation.getRejectionReason())
                .confirmationMessage(reservation.getConfirmationMessage())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
