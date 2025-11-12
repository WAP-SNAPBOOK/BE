package com.example.easybooking.errors.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
public class ErrorResponse {
    private final String code;
    private final String message;
}
