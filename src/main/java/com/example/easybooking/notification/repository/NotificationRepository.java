package com.example.easybooking.notification.repository;

import com.example.easybooking.notification.domain.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByIdDesc(Long recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndIdLessThanOrderByIdDesc(
            Long recipientId,
            Long cursor,
            Pageable pageable
    );

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    long countByRecipientIdAndReadAtIsNull(Long recipientId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification notification
               set notification.readAt = current_timestamp
             where notification.recipientId = :recipientId
               and notification.readAt is null
            """)
    int markAllAsRead(@Param("recipientId") Long recipientId);
}
