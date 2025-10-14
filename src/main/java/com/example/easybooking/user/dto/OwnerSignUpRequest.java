package com.example.easybooking.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OwnerSignUpRequest {
    private String name;
    private String phoneNumber;
}
