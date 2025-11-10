package com.example.easybooking.errors.response;

import com.example.easybooking.auth.RequireAuthenticatedUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class ErrorResponse {
    private final String code;
    private final String message;
}
