package com.example.easybooking.reservation.event;

import com.example.easybooking.chat.dto.response.MessageResponse;

public record ReservationMessageSavedEvent(
        Long chatRoomId,
        MessageResponse message
) {
}
