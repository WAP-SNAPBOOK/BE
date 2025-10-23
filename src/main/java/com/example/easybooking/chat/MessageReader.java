package com.example.easybooking.chat;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.repository.MessageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageReader {
    private final MessageRepository messageRepository;

    public Message read(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));
    }

    public List<Message> readMessages(ChatRoom chatRoom, Long cursorId, int size, Long userId) {
        List<Message> messages;
        if (cursorId == null) {
            messages = messageRepository.findLatestMessages(chatRoom.getId(), size);
            chatRoom.updateLastReadMessageId(messages.get(0).getId(), userId);
        } else {
            messages = messageRepository.findMessagesBeforeCursor(chatRoom.getId(), cursorId, size);
        }
        return messages;
    }


    public int countUnreadMessages(Long chatRoomId, Long lastMessageId, Long userId) {
        return messageRepository.countUnreadMessages(chatRoomId, lastMessageId, userId);
    }
}
