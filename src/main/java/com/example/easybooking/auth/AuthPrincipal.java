package com.example.easybooking.auth;

public sealed interface AuthPrincipal permits TempUser, AuthenticatedUser {

    boolean isTemporary();
}
