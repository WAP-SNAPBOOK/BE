package com.example.easybooking.chat.dto.response;

import com.example.easybooking.chat.domain.ReservationChangeSnapshot;
import java.util.List;

public record MenuSnapshotResponse(
        Long menuId,
        String menuName,
        String tagName,
        Long price,
        Integer sortOrder,
        List<MenuInputValueSnapshotResponse> inputValues
) {
    public static MenuSnapshotResponse from(ReservationChangeSnapshot.MenuSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new MenuSnapshotResponse(
                snapshot.menuId(),
                snapshot.menuName(),
                snapshot.tagName(),
                snapshot.price(),
                snapshot.sortOrder(),
                snapshot.inputValues() == null
                        ? List.of()
                        : snapshot.inputValues().stream()
                                .map(MenuInputValueSnapshotResponse::from)
                                .toList()
        );
    }
}
