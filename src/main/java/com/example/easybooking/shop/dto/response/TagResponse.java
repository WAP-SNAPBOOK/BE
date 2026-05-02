package com.example.easybooking.shop.dto.response;

import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.domain.Tag;
import lombok.Getter;

@Getter
public class TagResponse {

    private final Long id;
    private final String name;

    public TagResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TagResponse(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }

    public TagResponse(ShopTag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }
}
