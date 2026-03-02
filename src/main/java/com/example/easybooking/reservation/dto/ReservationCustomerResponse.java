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
public class ReservationCustomerResponse {
    private Long id;
    private String shopName;        // 샵 이름 (Shop 엔티티에서 조회)
    private String customerName;    // 고객 이름 (User 엔티티에서 조회)
    private Reservation.Status status;
    private LocalDate date;
    private LocalTime time;
    private int photoCount;          // 첨부 사진 갯수
    private List<String> photoUrls;  // 첨부 사진 URL 목록
    private String rejectionReason;      // 거절 사유 (거절 시)
    private String confirmationMessage;  // 전달 사항 (확정 시)
    private LocalDateTime createdAt;

    public static ReservationCustomerResponse from(
            Reservation reservation,
            String customerName,
            String shopName
    ) {
        List<String> photoUrls = reservation.getDesignImageURLs();
        int photoCount = photoUrls.size();

        return ReservationCustomerResponse.builder()
                .id(reservation.getId())
                .shopName(shopName)
                .customerName(customerName)
                .status(reservation.getStatus())
                .date(reservation.getDate())
                .time(reservation.getTime())
                .photoCount(photoCount)
                .photoUrls(photoUrls)
                .rejectionReason(reservation.getRejectionReason())
                .confirmationMessage(reservation.getConfirmationMessage())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
