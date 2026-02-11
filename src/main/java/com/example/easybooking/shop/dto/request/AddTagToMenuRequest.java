package com.example.easybooking.shop.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddTagToMenuRequest {

    @NotNull
    private Long tagId;

    public AddTagToMenuRequest(Long tagId) {
        this.tagId = tagId;
    }
}
