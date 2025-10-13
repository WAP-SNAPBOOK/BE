package com.example.easybooking.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.easybooking.chat.domain.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByShopIdAndCustomerId(Long shopId, Long customerId);
}
