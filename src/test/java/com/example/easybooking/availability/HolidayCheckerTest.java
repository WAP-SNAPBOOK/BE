package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.PublicHoliday;
import com.example.easybooking.availability.domain.ShopHoliday;
import com.example.easybooking.availability.domain.ShopSettings;
import com.example.easybooking.availability.repository.PublicHolidayRepository;
import com.example.easybooking.availability.repository.ShopHolidayRepository;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class HolidayCheckerTest {

    @Autowired
    ShopHolidayRepository shopHolidayRepository;

    @Autowired
    PublicHolidayRepository publicHolidayRepository;

    @Autowired
    ShopSettingsRepository shopSettingsRepository;

    @Test
    void isHoliday_returnsTrue_forWeeklyHoliday() {
        HolidayChecker holidayChecker = new HolidayChecker(
                shopHolidayRepository,
                publicHolidayRepository,
                shopSettingsRepository
        );
        shopHolidayRepository.saveAndFlush(ShopHoliday.createWeekly(1L, DayOfWeek.SUNDAY));

        boolean result = holidayChecker.isHoliday(1L, LocalDate.of(2026, 2, 15));

        assertThat(result).isTrue();
    }

    @Test
    void isHoliday_returnsFalse_forNonWeeklyHolidayDate() {
        HolidayChecker holidayChecker = new HolidayChecker(
                shopHolidayRepository,
                publicHolidayRepository,
                shopSettingsRepository
        );
        shopHolidayRepository.saveAndFlush(ShopHoliday.createWeekly(1L, DayOfWeek.SUNDAY));

        boolean result = holidayChecker.isHoliday(1L, LocalDate.of(2026, 2, 16));

        assertThat(result).isFalse();
    }

    @Test
    void isHoliday_returnsTrue_forBiweeklyHolidayOnOffWeek() {
        HolidayChecker holidayChecker = new HolidayChecker(
                shopHolidayRepository,
                publicHolidayRepository,
                shopSettingsRepository
        );
        shopHolidayRepository.saveAndFlush(
                ShopHoliday.createBiweekly(1L, DayOfWeek.SATURDAY, LocalDate.of(2026, 2, 14))
        );

        boolean result = holidayChecker.isHoliday(1L, LocalDate.of(2026, 2, 28));

        assertThat(result).isTrue();
    }

    @Test
    void isHoliday_returnsFalse_forBiweeklyHolidayOnAlternateWeek() {
        HolidayChecker holidayChecker = new HolidayChecker(
                shopHolidayRepository,
                publicHolidayRepository,
                shopSettingsRepository
        );
        shopHolidayRepository.saveAndFlush(
                ShopHoliday.createBiweekly(1L, DayOfWeek.SATURDAY, LocalDate.of(2026, 2, 14))
        );

        boolean result = holidayChecker.isHoliday(1L, LocalDate.of(2026, 2, 21));

        assertThat(result).isFalse();
    }

    @Test
    void isHoliday_returnsTrue_forMonthlyHoliday() {
        HolidayChecker holidayChecker = new HolidayChecker(
                shopHolidayRepository,
                publicHolidayRepository,
                shopSettingsRepository
        );
        shopHolidayRepository.saveAndFlush(ShopHoliday.createMonthly(1L, 2, DayOfWeek.MONDAY));

        boolean result = holidayChecker.isHoliday(1L, LocalDate.of(2026, 3, 9));

        assertThat(result).isTrue();
    }

    @Test
    void isHoliday_returnsTrue_forCustomHoliday() {
        HolidayChecker holidayChecker = new HolidayChecker(
                shopHolidayRepository,
                publicHolidayRepository,
                shopSettingsRepository
        );
        shopHolidayRepository.saveAndFlush(ShopHoliday.createCustom(1L, LocalDate.of(2026, 3, 15)));

        boolean result = holidayChecker.isHoliday(1L, LocalDate.of(2026, 3, 15));

        assertThat(result).isTrue();
    }

    @Test
    void isHoliday_returnsTrue_forPublicHoliday_whenPublicHolidayOffIsTrue() {
        HolidayChecker holidayChecker = new HolidayChecker(
                shopHolidayRepository,
                publicHolidayRepository,
                shopSettingsRepository
        );
        ShopSettings shopSettings = ShopSettings.createDefault(1L);
        ReflectionTestUtils.setField(shopSettings, "publicHolidayOff", true);
        shopSettingsRepository.saveAndFlush(shopSettings);
        publicHolidayRepository.saveAndFlush(PublicHoliday.create(LocalDate.of(2026, 3, 1), "삼일절"));

        boolean result = holidayChecker.isHoliday(1L, LocalDate.of(2026, 3, 1));

        assertThat(result).isTrue();
    }

    @Test
    void isHoliday_returnsFalse_forPublicHoliday_whenPublicHolidayOffIsFalse() {
        HolidayChecker holidayChecker = new HolidayChecker(
                shopHolidayRepository,
                publicHolidayRepository,
                shopSettingsRepository
        );
        shopSettingsRepository.saveAndFlush(ShopSettings.createDefault(1L));
        publicHolidayRepository.saveAndFlush(PublicHoliday.create(LocalDate.of(2026, 3, 1), "삼일절"));

        boolean result = holidayChecker.isHoliday(1L, LocalDate.of(2026, 3, 1));

        assertThat(result).isFalse();
    }
}
