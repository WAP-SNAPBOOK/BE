package com.example.easybooking.reservation.dto;

import com.example.easybooking.form.FormParsingUtil;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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

    private String part;
    private String removal;
    private String requests;

    private Integer extendCount;
    private Integer wrappingCount;

    private String extendStatus;    // 연장 유/무 (화면 표시용)
    private String wrappingStatus;  // 래핑 유/무 (화면 표시용)

    public static ReservationCustomerResponse from(
            Reservation reservation,
            UserReader userReader,
            ShopReader shopReader,
            Map<String, String> formData
    ) {
        // 1. 고객 이름 조회
        User customer = userReader.read(reservation.getCustomerId());
        String customerName = customer.getName();

        // 2. 샵 이름 조회
        Shop shop = shopReader.read(reservation.getShopId());
        String shopName = shop.getBusinessName();

        // 3. 사진 URL 목록 조회 및 개수 계산
        List<String> photoUrls = reservation.getDesignImageURLs();
        int photoCount = photoUrls.size();

        // 4. 폼 데이터에서 상세 필드 추출 및 계산
        String part = formData.get("part");
        String removal = formData.get("removal");
        String requests = formData.get("requests");

        Integer extendCount = FormParsingUtil.parseSafeInteger(formData.get("extend"));
        Integer wrappingCount = FormParsingUtil.parseSafeInteger(formData.get("wrapping"));

        String extendStatus = (extendCount != null && extendCount > 0) ? "유" : "무";
        String wrappingStatus = (wrappingCount != null && wrappingCount > 0) ? "유" : "무";

        return ReservationCustomerResponse.builder()
                .id(reservation.getId())
                .shopName(shopName)
                .customerName(customerName)
                .status(reservation.getStatus())
                .date(reservation.getDate())
                .time(reservation.getTime())
                .photoCount(photoCount)
                .photoUrls(reservation.getDesignImageURLs())
                .rejectionReason(reservation.getRejectionReason())
                .confirmationMessage(reservation.getConfirmationMessage())
                .createdAt(reservation.getCreatedAt())

                .part(part)
                .removal(removal)
                .requests(requests)
                .extendCount(extendCount)
                .wrappingCount(wrappingCount)
                .extendStatus(extendStatus)
                .wrappingStatus(wrappingStatus)
                .build();
    }
}
