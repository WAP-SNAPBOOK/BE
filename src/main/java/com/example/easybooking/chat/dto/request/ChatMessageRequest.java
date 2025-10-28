package com.example.easybooking.chat.dto.request;

import jakarta.validation.constraints.Size;
import java.awt.TrayIcon.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatMessageRequest {
    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다")
    String message;

    String imageUrl;

    MessageType messageType;

    public boolean hasText() {
        return message != null && !message.trim().isEmpty();
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }
}
