package com.example.easybooking.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public final class AuthenticatedUser implements AuthPrincipal {
    private final Long userId;
    private final String role;
    
    @Override
    public boolean isTemporary() {
        return false;
    }
}
