package com.example.easybooking.reservation.event;

import com.example.easybooking.chat.domain.MessageType;
import com.example.easybooking.chat.domain.ReservationChangeSnapshot;

public record ReservationEvent(
        Long reservationId,
        Long shopId,
        Long customerId,
        Long actorUserId,
        Integer durationMinutes,
        String content,
        ReservationChangeSnapshot reservationChange,
        MessageType messageType
) {
}
