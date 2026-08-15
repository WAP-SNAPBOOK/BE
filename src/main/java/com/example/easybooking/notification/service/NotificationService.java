package com.example.easybooking.notification.service;

import com.example.easybooking.errors.errorcode.NotificationErrorCode;
import com.example.easybooking.errors.exception.NotificationException;
import com.example.easybooking.notification.domain.Notification;
import com.example.easybooking.notification.dto.NotificationResponse;
import com.example.easybooking.notification.dto.UnreadNotificationCountResponse;
import com.example.easybooking.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long recipientId, Long cursor, int size) {
        PageRequest pageable = PageRequest.of(0, size);
        List<Notification> notifications = cursor == null
                ? notificationRepository.findByRecipientIdOrderByIdDesc(recipientId, pageable)
                : notificationRepository.findByRecipientIdAndIdLessThanOrderByIdDesc(
                        recipientId,
                        cursor,
                        pageable
                );

        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getUnreadCount(Long recipientId) {
        return new UnreadNotificationCountResponse(
                notificationRepository.countByRecipientIdAndReadAtIsNull(recipientId)
        );
    }

    @Transactional
    public void markAsRead(Long notificationId, Long recipientId) {
        Notification notification = notificationRepository
                .findByIdAndRecipientId(notificationId, recipientId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markAsRead(LocalDateTime.now());
    }

    @Transactional
    public void markAllAsRead(Long recipientId) {
        notificationRepository.markAllAsRead(recipientId);
    }
}
