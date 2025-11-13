package com.example.easybooking.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public final class TempUser implements AuthPrincipal {
    private final String providerId;
    
    @Override
    public boolean isTemporary() {
        return true;
    }
}
