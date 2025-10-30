package com.example.easybooking.link;

import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {
    private final ShopReader shopReader;

    public Shop resolveShop(String slugOrCode) {
        // slug 우선 → 없으면 코드
        try {
            return shopReader.readBySlug(slugOrCode);
        } catch (IllegalArgumentException ignore) {
            return shopReader.readByPublicCode(slugOrCode);
        }
    }
}
