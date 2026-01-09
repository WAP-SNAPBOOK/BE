package com.example.easybooking.reservation.event;

public record ReservationCreatedEvent(
        Long reservationId,
        Long shopId,
        Long customerId
) {}


