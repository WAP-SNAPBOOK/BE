package com.example.easybooking.chat.presentation;

import com.example.easybooking.auth.AuthenticatedUser;
import com.example.easybooking.auth.RequireAuthenticatedUser;
import com.example.easybooking.chat.dto.request.UpdateLastReadMessageRequest;
import com.example.easybooking.chat.dto.response.ChatRoomListResponse;
import com.example.easybooking.chat.dto.response.ChatRoomResponse;
import com.example.easybooking.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "Chat Room", description = "채팅방 관리 API")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    /**
     * 특정 샵의 채팅방 조회 또는 생성 정식 인증 토큰이 필요합니다.
     */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @PathVariable Long shopId,
            @RequireAuthenticatedUser AuthenticatedUser user) {
        return ResponseEntity.ok(
                chatRoomService.getChatRoom(shopId, user.getUserId())
        );
    }

    /**
     * 내 채팅방 목록 조회 정식 인증 토큰이 필요합니다.
     */
    @GetMapping("/")
    public ResponseEntity<List<ChatRoomListResponse>> getChatRoomList(
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(
                chatRoomService.getChatRoomList(user.getUserId())
        );
    }

    // ChatRoomController.java
    @PatchMapping("/{chatRoomId}/last-read-message")
    public ResponseEntity<Void> updateLastReadMessage(
            @PathVariable Long chatRoomId,
            @RequestBody UpdateLastReadMessageRequest request,
            @RequireAuthenticatedUser AuthenticatedUser user) {
        chatRoomService.updateLastReadMessage(
                chatRoomId,
                user.getUserId(),
                request.getLastReadMessageId()
        );
        return ResponseEntity.ok().build();
    }
}
