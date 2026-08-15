package com.example.easybooking.reservation.event;

import com.example.easybooking.notification.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationNotificationPublishListener {
    private final NotificationPublisher notificationPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationNotificationSaved(ReservationNotificationSavedEvent event) {
        try {
            notificationPublisher.publishToUser(event.recipientId(), event.notification());
        } catch (RuntimeException e) {
            log.error(
                    "예약 알림 웹소켓 발행 실패 recipientId={} notificationId={}",
                    event.recipientId(),
                    event.notification().notificationId(),
                    e
            );
        }
    }
}
