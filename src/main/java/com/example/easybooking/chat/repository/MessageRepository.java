package com.example.easybooking.chat.repository;

import com.example.easybooking.chat.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.chatRoomId = :chatRoomId " +
            "AND m.id > :lastReadMessageId " +
            "AND m.senderId <> :userId")
    int countUnreadMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("lastReadMessageId") Long lastReadMessageId,
            @Param("userId") Long userId
    );
}
