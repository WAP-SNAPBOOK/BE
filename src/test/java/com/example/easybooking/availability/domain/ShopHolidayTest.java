package com.example.easybooking.availability.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ShopHolidayTest {

    @Test
    void createWeekly_setsWeeklyFields() {
        ShopHoliday shopHoliday = ShopHoliday.createWeekly(1L, DayOfWeek.SUNDAY);

        assertThat(shopHoliday.getShopId()).isEqualTo(1L);
        assertThat(shopHoliday.getHolidayType()).isEqualTo(HolidayType.WEEKLY);
        assertThat(shopHoliday.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
    }

    @Test
    void createBiweekly_setsBiweeklyFields() {
        LocalDate referenceDate = LocalDate.of(2026, 2, 14);

        ShopHoliday shopHoliday = ShopHoliday.createBiweekly(1L, DayOfWeek.SATURDAY, referenceDate);

        assertThat(shopHoliday.getShopId()).isEqualTo(1L);
        assertThat(shopHoliday.getHolidayType()).isEqualTo(HolidayType.BIWEEKLY);
        assertThat(shopHoliday.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(shopHoliday.getReferenceDate()).isEqualTo(referenceDate);
    }

    @Test
    void createMonthly_setsMonthlyFields() {
        ShopHoliday shopHoliday = ShopHoliday.createMonthly(1L, 2, DayOfWeek.MONDAY);

        assertThat(shopHoliday.getShopId()).isEqualTo(1L);
        assertThat(shopHoliday.getHolidayType()).isEqualTo(HolidayType.MONTHLY);
        assertThat(shopHoliday.getWeekOfMonth()).isEqualTo(2);
        assertThat(shopHoliday.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void createCustom_setsCustomFields() {
        LocalDate specificDate = LocalDate.of(2026, 3, 15);

        ShopHoliday shopHoliday = ShopHoliday.createCustom(1L, specificDate);

        assertThat(shopHoliday.getShopId()).isEqualTo(1L);
        assertThat(shopHoliday.getHolidayType()).isEqualTo(HolidayType.CUSTOM);
        assertThat(shopHoliday.getSpecificDate()).isEqualTo(specificDate);
    }
}
