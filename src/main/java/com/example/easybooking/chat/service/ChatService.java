package com.example.easybooking.chat.service;

import com.example.easybooking.chat.ChatRoomReader;
import com.example.easybooking.chat.MessageWriter;
import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.dto.request.ChatMessageRequest;
import com.example.easybooking.chat.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final MessageWriter messageWriter;
    private final ChatRoomReader chatRoomReader;

    public MessageResponse saveMessage(Long chatRoomId, Long userId, ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomReader.read(chatRoomId);

        if (!chatRoom.isParticipant(userId)) {
            throw new IllegalArgumentException("해당 채팅방에 참여 권한이 없습니다.");
        }

        return messageWriter.save(chatRoomId, userId, request);
    }

    public Long extractUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken token =
                    (UsernamePasswordAuthenticationToken) principal;
            Object principalObj = token.getPrincipal();

            if (principalObj instanceof Long) {
                return (Long) principalObj;
            }
        }

        throw new IllegalStateException("유효하지 않은 인증 정보입니다.");
    }
}
