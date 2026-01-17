package com.example.easybooking.reservation.event;

import com.example.easybooking.chat.domain.MessageType;

public record ReservationEvent(
        Long reservationId,
        Long shopId,
        Long customerId,
        MessageType messageType
) {
}


