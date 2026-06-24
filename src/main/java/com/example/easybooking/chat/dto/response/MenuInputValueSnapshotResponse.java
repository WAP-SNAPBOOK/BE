package com.example.easybooking.chat.dto.response;

import com.example.easybooking.chat.domain.ReservationChangeSnapshot;
import java.math.BigDecimal;

public record MenuInputValueSnapshotResponse(
        Long inputFieldId,
        String fieldLabel,
        String inputType,
        BigDecimal valueNumber,
        String valueText
) {
    public static MenuInputValueSnapshotResponse from(
            ReservationChangeSnapshot.MenuInputValueSnapshot snapshot
    ) {
        if (snapshot == null) {
            return null;
        }
        return new MenuInputValueSnapshotResponse(
                snapshot.inputFieldId(),
                snapshot.fieldLabel(),
                snapshot.inputType(),
                snapshot.valueNumber(),
                snapshot.valueText()
        );
    }
}
