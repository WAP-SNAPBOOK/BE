package com.example.easybooking.link;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.chat.dto.response.ChatRoomResponse;
import com.example.easybooking.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/link")
@RequiredArgsConstructor
@Tag(name = "Link", description = "공개 링크 API")
public class LinkController {
    private final LinkService linkService;
    private final ChatRoomService chatRoomService;


    @GetMapping("/chat/{slugOrCode}")
    public ResponseEntity<ChatRoomResponse> openChat(
            @PathVariable String slugOrCode,
            @RequireAuthenticatedUser AuthenticatedUser user
    ) {
        return ResponseEntity.ok(linkService.resolveChatRoom(slugOrCode,user.getUserId()));
    }
}