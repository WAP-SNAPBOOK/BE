package com.example.easybooking.chat;

import com.example.easybooking.chat.request.ChatRequest;
import com.example.easybooking.chat.response.ChatResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public ChatResponse sendMessage(ChatRequest request) {
        return new ChatResponse("Message received: " + request.getMessage());
    }
}
