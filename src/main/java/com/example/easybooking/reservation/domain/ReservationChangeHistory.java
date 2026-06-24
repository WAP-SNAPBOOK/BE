package com.example.easybooking.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "reservation_change_histories",
        indexes = {
                @Index(name = "idx_rch_reservation", columnList = "reservation_id, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "before_json", columnDefinition = "LONGTEXT")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "LONGTEXT")
    private String afterJson;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ReservationChangeHistory create(
            Long reservationId,
            Long changedByUserId,
            String changeType,
            String beforeJson,
            String afterJson,
            String reason
    ) {
        ReservationChangeHistory history = new ReservationChangeHistory();
        history.reservationId = reservationId;
        history.changedByUserId = changedByUserId;
        history.changeType = changeType;
        history.beforeJson = beforeJson;
        history.afterJson = afterJson;
        history.reason = reason;
        return history;
    }
}
