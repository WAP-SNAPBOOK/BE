package com.example.easybooking.availability.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ShopOperatingTimeTest {

    @Test
    void create_returnsEntityWithFields() {
        ShopOperatingTime shopOperatingTime = ShopOperatingTime.create(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(19, 0)
        );

        assertThat(shopOperatingTime.getShopId()).isEqualTo(1L);
        assertThat(shopOperatingTime.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(shopOperatingTime.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(shopOperatingTime.getEndTime()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void create_throwsException_whenStartTimeIsAfterEndTime() {
        assertThatThrownBy(() -> ShopOperatingTime.create(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(19, 0),
                LocalTime.of(10, 0)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_allowsEqualStartAndEndTime() {
        ShopOperatingTime shopOperatingTime = ShopOperatingTime.create(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(10, 0)
        );

        assertThat(shopOperatingTime.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(shopOperatingTime.getEndTime()).isEqualTo(LocalTime.of(10, 0));
    }
}
