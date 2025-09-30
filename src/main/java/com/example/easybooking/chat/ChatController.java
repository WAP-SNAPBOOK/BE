package com.example.easybooking.chat;

import com.example.easybooking.chat.request.ChatMessageRequest;
import com.example.easybooking.chat.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/{chatRoomId}")
    @SendTo("/topic/chat/{chatRoomId}")
    public ChatMessageResponse sendMessage(
            @DestinationVariable Long chatRoomId,
            ChatMessageRequest request,
            @AuthenticationPrincipal String providerId) {

        return chatService.saveMessage(chatRoomId, providerId, request);
    }
}
