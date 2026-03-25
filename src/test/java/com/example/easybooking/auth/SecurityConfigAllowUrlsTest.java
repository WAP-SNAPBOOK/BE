package com.example.easybooking.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecurityConfigAllowUrlsTest {

    @Test
    void allowUrls_containsPublicBookingEntryPath() {
        assertThat(SecurityConfig.allowUrls)
                .contains("/api/public/shops/*/booking-entry");
    }
}
