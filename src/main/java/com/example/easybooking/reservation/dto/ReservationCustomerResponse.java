package com.example.easybooking.reservation.dto;

import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ReservationCustomerResponse {
    private Long id;
    private String shopName;        // 샵 이름 (Shop 엔티티에서 조회)
    private String customerName;    // 고객 이름 (User 엔티티에서 조회)
    private Reservation.Status status;
    private LocalDate date;
    private LocalTime time;
    private int photoCount;         // 첨부 사진 갯수

    public static ReservationCustomerResponse from(Reservation reservation, UserReader userReader, ShopReader shopReader) {
        // 1. 고객 이름 조회
        User customer = userReader.read(reservation.getCustomerId());
        String customerName = customer.getName();

        // 2. 샵 이름 조회
        Shop shop = shopReader.read(reservation.getShopId());
        String shopName = shop.getBusinessName();

        // 3. 사진 개수 계산
        int photoCount = reservation.getDesignImageURLs().size();

        return ReservationCustomerResponse.builder()
                .id(reservation.getId())
                .shopName(shopName)
                .customerName(customerName)
                .status(reservation.getStatus())
                .date(reservation.getDate())
                .time(reservation.getTime())
                .photoCount(photoCount)
                .build();
    }
}
