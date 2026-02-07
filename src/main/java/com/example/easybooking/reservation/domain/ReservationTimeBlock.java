package com.example.easybooking.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "reservation_time_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationTimeBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "block_start_at", nullable = false)
    private LocalDateTime blockStartAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ReservationTimeBlock create(Long reservationId, Long staffId, LocalDateTime blockStartAt) {
        ReservationTimeBlock block = new ReservationTimeBlock();
        block.reservationId = reservationId;
        block.staffId = staffId;
        block.blockStartAt = blockStartAt;
        block.createdAt = LocalDateTime.now();
        return block;
    }
}

