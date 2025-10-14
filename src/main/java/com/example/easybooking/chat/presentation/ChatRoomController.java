package com.example.easybooking.chat.presentation;

import com.example.easybooking.chat.dto.response.ChatRoomListResponse;
import com.example.easybooking.chat.dto.response.ChatRoomResponse;
import com.example.easybooking.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "Chat Room", description = "채팅방 관리 API")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @GetMapping("shop/{shopId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long shopId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(chatRoomService.getChatRoom(shopId, userId));
    }

    @GetMapping("/")
    public ResponseEntity<List<ChatRoomListResponse>> getChatRoomList(
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(chatRoomService.getChatRoomList(userId));
    }
}
