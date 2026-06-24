package com.example.easybooking.chat.domain;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

public record ReservationChangeSnapshot(
        ValueChange<LocalDate> date,
        ValueChange<LocalTime> startAt,
        ValueChange<Integer> durationMinutes,
        StaffChange staff,
        ValueChange<String> ownerMessage,
        ValueChange<List<MenuSnapshot>> menus
) {
    public record ValueChange<T>(T before, T after) {
    }

    public record StaffChange(StaffSnapshot before, StaffSnapshot after) {
    }

    public record StaffSnapshot(Long staffId, String staffName) {
    }

    public record MenuSnapshot(
            Long menuId,
            String menuName,
            String tagName,
            Long price,
            Integer sortOrder,
            List<MenuInputValueSnapshot> inputValues
    ) {
    }

    public record MenuInputValueSnapshot(
            Long inputFieldId,
            String fieldLabel,
            String inputType,
            BigDecimal valueNumber,
            String valueText
    ) {
    }
}
