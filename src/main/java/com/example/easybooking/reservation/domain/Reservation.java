package com.example.easybooking.reservation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shopId;       // 현재는 원장님 User.id (추후 Shop Id로 확장 가능성)

    @Column(nullable = false)
    private Long ownerUserId;  // 담당 원장님 User.id (현재 shopId와 동일)

    @Column(nullable = false)
    private Long customerId;   // 고객 User.id

    // --- 예약 정보 ---
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    private String designImageURL;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;     // 예약 상태

    public enum Status {
        PENDING,       // 대기 (고객 신청, 원장 확인 전)
        CONFIRMED,     // 확정 (원장 수락)
        CANCELED,      // 취소 (고객 또는 원장 취소)
        REJECTED       // 거절 (원장 거절)
    }

    public static Reservation createReservation(
            Long shopId,
            Long ownerUserId,
            Long customerId,
            LocalDate date,
            LocalTime time,
            String designImageURL) {

        Reservation reservation = new Reservation();

        reservation.shopId = shopId;
        reservation.ownerUserId = ownerUserId;
        reservation.customerId = customerId;
        reservation.date = date;
        reservation.time = time;
        reservation.designImageURL = designImageURL;
        reservation.status = Status.PENDING;  // 초기 상태 : 대기
        reservation.createdAt = LocalDateTime.now();

        return reservation;
    }


    // TODO: 예약 상태 변경 함수 (confirm(), cancel(), reject())


}
