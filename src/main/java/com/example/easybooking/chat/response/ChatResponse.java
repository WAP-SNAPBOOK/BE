package com.example.easybooking.chat.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ChatResponse {
    String responseMessage;

    public ChatResponse(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
