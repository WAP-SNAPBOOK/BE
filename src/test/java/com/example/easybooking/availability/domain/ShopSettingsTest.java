package com.example.easybooking.availability.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.errors.errorcode.AvailabilityErrorCode;
import com.example.easybooking.errors.exception.AvailabilityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShopSettingsTest {

    @Test
    @DisplayName("기본 설정 생성 시 비즈니스 기본값을 반환한다")
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
    @DisplayName("예약 간격이 30 또는 60이 아니면 AvailabilityException을 던진다")
    void updateInterval_throwsAvailabilityException_whenIntervalIsNot30Or60() {
        ShopSettings settings = ShopSettings.createDefault(1L);

        assertThatThrownBy(() -> settings.updateInterval(0))
                .isInstanceOf(AvailabilityException.class)
                .satisfies(e -> {
                    AvailabilityException ex = (AvailabilityException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(AvailabilityErrorCode.INVALID_INTERVAL_MINUTES);
                });

        assertThatThrownBy(() -> settings.updateInterval(-10))
                .isInstanceOf(AvailabilityException.class)
                .satisfies(e -> {
                    AvailabilityException ex = (AvailabilityException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(AvailabilityErrorCode.INVALID_INTERVAL_MINUTES);
                });

        assertThatThrownBy(() -> settings.updateInterval(15))
                .isInstanceOf(AvailabilityException.class)
                .satisfies(e -> {
                    AvailabilityException ex = (AvailabilityException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(AvailabilityErrorCode.INVALID_INTERVAL_MINUTES);
                });
    }

    @Test
    @DisplayName("예약 간격은 30 또는 60만 허용한다")
    void updateInterval_acceptsOnly30Or60() {
        ShopSettings settings = ShopSettings.createDefault(1L);

        settings.updateInterval(30);
        assertThat(settings.getIntervalMinutes()).isEqualTo(30);

        settings.updateInterval(60);
        assertThat(settings.getIntervalMinutes()).isEqualTo(60);
    }
}
