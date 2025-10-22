package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageReader {
    private final MessageRepository messageRepository;

    public Message read(Long messageId){
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));
    }

    public List<MessageResponse> readMessages(Long chatRoomId, Long cursorId, int size) {
        List<Message> messages;
        if (cursorId == null) {
            messages = messageRepository.findLatestMessages(chatRoomId, size);
        } else {
            messages = messageRepository.findMessagesBeforeCursor(chatRoomId, cursorId, size);
        }
        return messages.stream()
                .map(MessageResponse::from)
                .toList();
    }


    public int countUnreadMessages(Long chatRoomId, Long lastMessageId,Long userId) {
        return messageRepository.countUnreadMessages(chatRoomId, lastMessageId, userId);
    }
}
