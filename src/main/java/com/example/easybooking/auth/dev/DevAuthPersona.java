package com.example.easybooking.auth.dev;

import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.user.domain.UserType;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DevAuthPersona {
    OWNER_1("owner-1", "dev-owner-1001", UserType.OWNER, "기존 원장 계정 확인용 persona"),
    CUSTOMER_1("customer-1", "dev-customer-2001", UserType.CUSTOMER, "기존 고객 계정 확인용 persona"),
    CUSTOMER_2("customer-2", "dev-customer-2002", UserType.CUSTOMER, "추가 고객 계정 확인용 persona"),
    NEW_OWNER_1("new-owner-1", "dev-owner-new-3001", UserType.OWNER, "회원가입 필요 원장 persona"),
    NEW_CUSTOMER_1("new-customer-1", "dev-customer-new-4001", UserType.CUSTOMER, "회원가입 필요 고객 persona");

    private final String key;
    private final String providerId;
    private final UserType userType;
    private final String description;

    public static DevAuthPersona fromKey(String personaKey) {
        return Arrays.stream(values())
                .filter(persona -> persona.key.equals(personaKey))
                .findFirst()
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_DEV_AUTH_PERSONA, personaKey));
    }

    public static List<DevAuthPersona> all() {
        return List.of(values());
    }
}
