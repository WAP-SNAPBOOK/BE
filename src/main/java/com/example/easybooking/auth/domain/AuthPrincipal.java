package com.example.easybooking.auth.domain;

public sealed interface AuthPrincipal permits TempUser, AuthenticatedUser {

    boolean isTemporary();
}
