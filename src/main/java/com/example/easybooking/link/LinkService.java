package com.example.easybooking.link;

import com.example.easybooking.chat.dto.response.ChatRoomResponse;
import com.example.easybooking.chat.service.ChatRoomService;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {
    private final ShopReader shopReader;
    private final ChatRoomService chatRoomService;

    public Shop resolveShop(String slugOrCode) {
        return shopReader.readBySlugOrPublicCode(slugOrCode);
    }

    public ChatRoomResponse resolveChatRoom(String slugOrCode, Long userId) {
        Shop shop =  resolveShop(slugOrCode);
        return chatRoomService.getChatRoom(shop.getId(), userId);
    }
}
