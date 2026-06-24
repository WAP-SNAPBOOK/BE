package com.example.easybooking.chat.dto.response;

import com.example.easybooking.chat.domain.ReservationChangeSnapshot;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReservationChangeResponse(
        ValueChangeResponse<LocalDate> date,
        ValueChangeResponse<String> startAt,
        ValueChangeResponse<Integer> durationMinutes,
        StaffChangeResponse staff,
        ValueChangeResponse<String> ownerMessage,
        ValueChangeResponse<List<MenuSnapshotResponse>> menus
) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static ReservationChangeResponse from(ReservationChangeSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        return new ReservationChangeResponse(
                ValueChangeResponse.from(snapshot.date()),
                snapshot.startAt() == null
                        ? null
                        : new ValueChangeResponse<>(
                                formatTime(snapshot.startAt().before()),
                                formatTime(snapshot.startAt().after())
                        ),
                snapshot.durationMinutes() == null
                        ? null
                        : new ValueChangeResponse<>(
                                snapshot.durationMinutes().before(),
                                snapshot.durationMinutes().after()
                        ),
                snapshot.staff() == null
                        ? null
                        : new StaffChangeResponse(
                                toResponse(snapshot.staff().before()),
                                toResponse(snapshot.staff().after())
                        ),
                ValueChangeResponse.from(snapshot.ownerMessage()),
                snapshot.menus() == null
                        ? null
                        : new ValueChangeResponse<>(
                                toMenuResponses(snapshot.menus().before()),
                                toMenuResponses(snapshot.menus().after())
                        )
        );
    }

    private static StaffSnapshotResponse toResponse(ReservationChangeSnapshot.StaffSnapshot snapshot) {
        return snapshot == null ? null : new StaffSnapshotResponse(snapshot.staffId(), snapshot.staffName());
    }

    private static String formatTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }

    private static List<MenuSnapshotResponse> toMenuResponses(
            List<ReservationChangeSnapshot.MenuSnapshot> snapshots
    ) {
        if (snapshots == null) {
            return List.of();
        }
        return snapshots.stream()
                .map(MenuSnapshotResponse::from)
                .toList();
    }
}
