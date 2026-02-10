package com.example.easybooking.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateTagRequest {

    @NotBlank
    private String name;

    public CreateTagRequest(String name) {
        this.name = name;
    }
}
