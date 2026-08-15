package com.example.easybooking.notification.domain;

import com.example.easybooking.chat.domain.MessageType;

public enum NotificationType {
    RESERVATION_CREATED("새 예약이 접수되었습니다."),
    RESERVATION_CONFIRMED("예약이 확정되었습니다."),
    RESERVATION_REJECTED("예약이 거절되었습니다."),
    RESERVATION_UPDATED("예약이 수정되었습니다."),
    RESERVATION_CANCELED("예약이 취소되었습니다.");

    private final String title;

    NotificationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static NotificationType from(MessageType messageType) {
        return switch (messageType) {
            case RESERVATION_CREATED -> RESERVATION_CREATED;
            case RESERVATION_CONFIRMED -> RESERVATION_CONFIRMED;
            case RESERVATION_REJECTED -> RESERVATION_REJECTED;
            case RESERVATION_UPDATED -> RESERVATION_UPDATED;
            case RESERVATION_CANCELED -> RESERVATION_CANCELED;
            default -> throw new IllegalArgumentException("알림을 지원하지 않는 메시지 유형입니다: " + messageType);
        };
    }
}
