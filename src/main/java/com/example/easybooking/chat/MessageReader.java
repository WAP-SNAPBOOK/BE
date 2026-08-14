package com.example.easybooking.chat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.errors.errorcode.ChatErrorCode;
import com.example.easybooking.errors.exception.ChatException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageReader {
    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public Message read(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.MESSAGE_NOT_FOUND));
    }

    public List<Message> readMessages(ChatRoom chatRoom, Long cursorId, int size, Long userId) {
        List<Message> messages;
        if (cursorId == null) {
            messages = messageRepository.findLatestMessages(chatRoom.getId(), size);
            if (!messages.isEmpty()) {
                chatRoom.updateLastReadMessageId(messages.get(0).getId(), userId);
                chatRoomRepository.save(chatRoom);
            }
        } else {
            messages = messageRepository.findMessagesBeforeCursor(chatRoom.getId(), cursorId, size);
        }
        return messages;
    }

    public List<Message> readMessagesAfter(ChatRoom chatRoom, Long afterMessageId, int size, Long userId) {
        List<Message> messages = messageRepository.findMessagesAfter(chatRoom.getId(), afterMessageId, size);
        if (!messages.isEmpty()) {
            Long lastMessageId = messages.get(messages.size() - 1).getId();
            chatRoom.updateLastReadMessageId(lastMessageId, userId);
            chatRoomRepository.save(chatRoom);
        }
        return messages;
    }


    public int countUnreadMessages(Long chatRoomId, Long lastReadMessageId, Long userId) {
        return messageRepository.countUnreadMessages(chatRoomId, lastReadMessageId, userId);
    }
}
