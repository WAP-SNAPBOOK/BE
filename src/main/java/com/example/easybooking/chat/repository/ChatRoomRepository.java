package com.example.easybooking.chat.repository;

import com.example.easybooking.chat.domain.ChatRoom;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByShopIdAndCustomerId(Long shopId, Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.id = :chatRoomId")
    Optional<ChatRoom> findByIdForUpdate(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.ownerId = :userId OR cr.customerId = :userId " +
            "ORDER BY cr.lastMessageAt DESC NULLS LAST")
    List<ChatRoom> findChatRooms(@Param("userId") Long userId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ChatRoom cr " +
            "SET cr.ownerLastReadMessageId = :messageId " +
            "WHERE cr.id = :chatRoomId " +
            "AND cr.ownerId = :userId " +
            "AND COALESCE(cr.ownerLastReadMessageId, 0) < :messageId")
    int advanceOwnerLastReadMessageId(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId,
            @Param("messageId") Long messageId
    );

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ChatRoom cr " +
            "SET cr.customerLastReadMessageId = :messageId " +
            "WHERE cr.id = :chatRoomId " +
            "AND cr.customerId = :userId " +
            "AND COALESCE(cr.customerLastReadMessageId, 0) < :messageId")
    int advanceCustomerLastReadMessageId(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId,
            @Param("messageId") Long messageId
    );

    void deleteByShopId(Long shopId);
    List<ChatRoom> findByShopId(Long shopId);
    void deleteByOwnerIdOrCustomerId(Long ownerId, Long customerId);
}
