package com.example.easybooking.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.easybooking.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByShopIdAndCustomerId(Long shopId, Long customerId);
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE cr.ownerId = :userId OR cr.customerId = :userId " +
            "ORDER BY cr.lastMessageAt DESC NULLS LAST")
    List<ChatRoom> findChatRooms(@Param("userId") Long userId);

    void deleteByShopId(Long shopId);
    List<ChatRoom> findByShopId(Long shopId);
    void deleteByOwnerIdOrCustomerId(Long ownerId, Long customerId);
}
