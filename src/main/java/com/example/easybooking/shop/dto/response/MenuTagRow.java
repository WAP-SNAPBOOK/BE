package com.example.easybooking.shop.dto.response;

public record MenuTagRow(
        Long menuId,
        Long tagId,
        String tagName,
        Integer sortOrder
) {
}
