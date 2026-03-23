package com.example.easybooking.shop.dto.response;

import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.domain.Tag;
import lombok.Getter;

@Getter
public class TagResponse {

    private final Long id;
    private final String name;

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }

    public TagResponse(ShopTag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }
}
