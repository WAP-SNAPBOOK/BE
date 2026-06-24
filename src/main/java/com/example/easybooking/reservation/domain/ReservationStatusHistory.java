package com.example.easybooking.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
        name = "reservation_status_histories",
        indexes = {
                @Index(name = "idx_rsh_reservation", columnList = "reservation_id, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private Reservation.Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private Reservation.Status toStatus;

    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ReservationStatusHistory create(
            Long reservationId,
            Reservation.Status fromStatus,
            Reservation.Status toStatus,
            Long changedByUserId,
            String reason
    ) {
        ReservationStatusHistory history = new ReservationStatusHistory();
        history.reservationId = reservationId;
        history.fromStatus = fromStatus;
        history.toStatus = toStatus;
        history.changedByUserId = changedByUserId;
        history.reason = reason;
        return history;
    }
}
