package com.example.easybooking.shop.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SlugUpdateRequest {
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[a-z0-9-]+$")
    private String slug;
}
