package com.example.easybooking.chat.dto.response;

import com.example.easybooking.chat.domain.ReservationChangeSnapshot;

public record ValueChangeResponse<T>(T before, T after) {
    public static <T> ValueChangeResponse<T> from(ReservationChangeSnapshot.ValueChange<T> change) {
        return change == null ? null : new ValueChangeResponse<>(change.before(), change.after());
    }
}
