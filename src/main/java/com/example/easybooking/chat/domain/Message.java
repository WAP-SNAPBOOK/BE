package com.example.easybooking.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Message {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<ReservationChangeSnapshot.MenuSnapshot>> MENU_SNAPSHOT_LIST_TYPE =
            new TypeReference<>() {
            };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private Long senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private MessageType messageType;

    @Column(nullable = true)
    private Long reservationId;

    @Column(nullable = true)
    private Integer durationMinutes;

    @Column(name = "reservation_start_at_before")
    private LocalTime reservationStartAtBefore;

    @Column(name = "reservation_start_at_after")
    private LocalTime reservationStartAtAfter;

    @Column(name = "reservation_date_before")
    private LocalDate reservationDateBefore;

    @Column(name = "reservation_date_after")
    private LocalDate reservationDateAfter;

    @Column(name = "reservation_duration_minutes_before")
    private Integer reservationDurationMinutesBefore;

    @Column(name = "reservation_duration_minutes_after")
    private Integer reservationDurationMinutesAfter;

    @Column(name = "reservation_staff_id_before")
    private Long reservationStaffIdBefore;

    @Column(name = "reservation_staff_name_before", length = 100)
    private String reservationStaffNameBefore;

    @Column(name = "reservation_staff_id_after")
    private Long reservationStaffIdAfter;

    @Column(name = "reservation_staff_name_after", length = 100)
    private String reservationStaffNameAfter;

    @Column(name = "owner_message", columnDefinition = "TEXT")
    private String ownerMessage;

    @Column(name = "owner_message_before", columnDefinition = "TEXT")
    private String ownerMessageBefore;

    @Column(name = "reservation_menus_before", columnDefinition = "TEXT")
    private String reservationMenusBefore;

    @Column(name = "reservation_menus_after", columnDefinition = "TEXT")
    private String reservationMenusAfter;

    public static Message create(Long chatRoomId, Long senderId, String content) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.content = content;
        message.sentAt = LocalDateTime.now();
        message.messageType = MessageType.TEXT;
        return message;
    }

    public static Message createImageMessgae(Long chatRoomId, Long senderId, String imageUrl) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.imageUrl = imageUrl;
        message.sentAt = LocalDateTime.now();
        message.messageType = MessageType.IMAGE;
        return message;
    }

    public static Message createImageWithTextMessage(Long chatRoomId, Long senderId, String content, String imageUrl) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.content = content;
        message.imageUrl = imageUrl;
        message.sentAt = LocalDateTime.now();
        message.messageType = MessageType.TEXT_IMAGE;
        return message;
    }

    public static Message createReservationSystemMessage(
            Long chatRoomId,
            Long systemSenderId,
            Long reservationId,
            Integer durationMinutes,
            String content,
            ReservationChangeSnapshot reservationChange,
            MessageType messageType
    ) {
        Message message = new Message();
        message.chatRoomId = chatRoomId;
        message.senderId = systemSenderId;
        message.content = content;
        message.reservationId = reservationId;
        message.durationMinutes = durationMinutes;
        message.applyReservationChange(reservationChange);
        message.sentAt = LocalDateTime.now();
        message.messageType = messageType;
        return message;
    }

    private void applyReservationChange(ReservationChangeSnapshot reservationChange) {
        if (reservationChange == null) {
            return;
        }
        if (reservationChange.date() != null) {
            this.reservationDateBefore = reservationChange.date().before();
            this.reservationDateAfter = reservationChange.date().after();
        }
        if (reservationChange.startAt() != null) {
            this.reservationStartAtBefore = reservationChange.startAt().before();
            this.reservationStartAtAfter = reservationChange.startAt().after();
        }
        if (reservationChange.durationMinutes() != null) {
            this.reservationDurationMinutesBefore = reservationChange.durationMinutes().before();
            this.reservationDurationMinutesAfter = reservationChange.durationMinutes().after();
        }
        if (reservationChange.staff() != null) {
            if (reservationChange.staff().before() != null) {
                this.reservationStaffIdBefore = reservationChange.staff().before().staffId();
                this.reservationStaffNameBefore = reservationChange.staff().before().staffName();
            }
            if (reservationChange.staff().after() != null) {
                this.reservationStaffIdAfter = reservationChange.staff().after().staffId();
                this.reservationStaffNameAfter = reservationChange.staff().after().staffName();
            }
        }
        if (reservationChange.ownerMessage() != null) {
            this.ownerMessageBefore = reservationChange.ownerMessage().before();
            this.ownerMessage = reservationChange.ownerMessage().after();
        }
        if (reservationChange.menus() != null) {
            this.reservationMenusBefore = writeMenuSnapshots(reservationChange.menus().before());
            this.reservationMenusAfter = writeMenuSnapshots(reservationChange.menus().after());
        }
    }

    public ReservationChangeSnapshot getReservationChangeSnapshot() {
        ReservationChangeSnapshot.ValueChange<LocalTime> startAtChange =
                reservationStartAtBefore == null && reservationStartAtAfter == null
                        ? null
                        : new ReservationChangeSnapshot.ValueChange<>(reservationStartAtBefore, reservationStartAtAfter);
        ReservationChangeSnapshot.ValueChange<LocalDate> dateChange =
                reservationDateBefore == null && reservationDateAfter == null
                        ? null
                        : new ReservationChangeSnapshot.ValueChange<>(reservationDateBefore, reservationDateAfter);
        ReservationChangeSnapshot.ValueChange<Integer> durationChange =
                reservationDurationMinutesBefore == null && reservationDurationMinutesAfter == null
                        ? null
                        : new ReservationChangeSnapshot.ValueChange<>(
                                reservationDurationMinutesBefore,
                                reservationDurationMinutesAfter
                        );
        ReservationChangeSnapshot.StaffChange staffChange =
                reservationStaffIdBefore == null && reservationStaffNameBefore == null
                        && reservationStaffIdAfter == null && reservationStaffNameAfter == null
                        ? null
                        : new ReservationChangeSnapshot.StaffChange(
                                new ReservationChangeSnapshot.StaffSnapshot(
                                        reservationStaffIdBefore,
                                        reservationStaffNameBefore
                                ),
                                new ReservationChangeSnapshot.StaffSnapshot(
                                        reservationStaffIdAfter,
                                        reservationStaffNameAfter
                                )
                        );
        ReservationChangeSnapshot.ValueChange<String> ownerMessageChange =
                ownerMessageBefore == null && ownerMessage == null
                        ? null
                        : new ReservationChangeSnapshot.ValueChange<>(ownerMessageBefore, ownerMessage);
        ReservationChangeSnapshot.ValueChange<List<ReservationChangeSnapshot.MenuSnapshot>> menusChange =
                reservationMenusBefore == null && reservationMenusAfter == null
                        ? null
                        : new ReservationChangeSnapshot.ValueChange<>(
                                readMenuSnapshots(reservationMenusBefore),
                                readMenuSnapshots(reservationMenusAfter)
                        );

        if (dateChange == null && startAtChange == null && durationChange == null
                && staffChange == null && ownerMessageChange == null && menusChange == null) {
            return null;
        }
        return new ReservationChangeSnapshot(
                dateChange,
                startAtChange,
                durationChange,
                staffChange,
                ownerMessageChange,
                menusChange
        );
    }

    private String writeMenuSnapshots(List<ReservationChangeSnapshot.MenuSnapshot> snapshots) {
        try {
            return OBJECT_MAPPER.writeValueAsString(snapshots == null ? List.of() : snapshots);
        } catch (Exception e) {
            throw new IllegalStateException("예약 메뉴 변경 스냅샷 직렬화에 실패했습니다.", e);
        }
    }

    private List<ReservationChangeSnapshot.MenuSnapshot> readMenuSnapshots(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, MENU_SNAPSHOT_LIST_TYPE);
        } catch (Exception e) {
            return List.of();
        }
    }
}
