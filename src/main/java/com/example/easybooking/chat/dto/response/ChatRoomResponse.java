package com.example.easybooking.chat.dto.response;

import com.example.easybooking.chat.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChatRoomResponse {
    private Long roomId;
    private Long shopId;
    private Boolean isNewRoom;

    public static ChatRoomResponse of(ChatRoom chatRoom,boolean isNewRoom) {
        return ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .shopId(chatRoom.getShopId())
                .isNewRoom(isNewRoom)
                .build();
    }
}
