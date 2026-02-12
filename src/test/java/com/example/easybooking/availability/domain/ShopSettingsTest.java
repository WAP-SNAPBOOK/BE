package com.example.easybooking.availability.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShopSettingsTest {

    @Test
    void createDefault_returnsBusinessDefaults() {
        ShopSettings settings = ShopSettings.createDefault(1L);

        assertThat(settings.getShopId()).isEqualTo(1L);
        assertThat(settings.getIntervalMinutes()).isEqualTo(30);
        assertThat(settings.getScheduleType()).isEqualTo(ScheduleType.DAILY);
        assertThat(settings.getBookingWindowDays()).isEqualTo(30);
        assertThat(settings.getMinBookingLeadMinutes()).isEqualTo(60);
        assertThat(settings.isPublicHolidayOff()).isFalse();
    }

    @Test
    void updateInterval_updatesIntervalMinutes() {
        ShopSettings settings = ShopSettings.createDefault(1L);

        settings.updateInterval(60);

        assertThat(settings.getIntervalMinutes()).isEqualTo(60);
    }
}
