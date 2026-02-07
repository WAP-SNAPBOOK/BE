package com.example.easybooking.reservation.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    //private String designImageURL;
    @ElementCollection
    @CollectionTable(name = "reservation_photos", joinColumns = @JoinColumn(name = "reservation_id"))
    @Column(name = "photo_url")
    private List<String> designImageURLs = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String rejectionReason;
    private String confirmationMessage;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String formDataJson;

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
            String formDataJson,
            List<String> designImageURLs) {

        Reservation reservation = new Reservation();

        reservation.shopId = shopId;
        reservation.ownerUserId = ownerUserId;
        reservation.customerId = customerId;
        reservation.date = date;
        reservation.time = time;
        reservation.startAt = LocalDateTime.of(date, time);
        reservation.formDataJson = formDataJson;
        reservation.designImageURLs = designImageURLs;
        reservation.status = Status.PENDING;  // 초기 상태 : 대기
        reservation.createdAt = LocalDateTime.now();

        return reservation;
    }

    public void confirm(String message, Integer durationMinutes) {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("대기 상태의 예약만 확정할 수 있습니다.");
        }
        this.confirmationMessage = message;
        this.durationMinutes = durationMinutes;
        this.status = Status.CONFIRMED;
    }

    public void reject(String reason) {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("대기 상태의 예약만 거절할 수 있습니다.");
        }
        this.rejectionReason = reason;
        this.status = Status.REJECTED;
    }

    public void cancel() {
        if (this.status == Status.CANCELED || this.status == Status.REJECTED) {
            throw new IllegalStateException("이미 취소 or 거절된 예약은 변경할 수 없습니다.");
        }
        this.status = Status.CANCELED;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
