package com.example.easybooking.availability.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.ShopHoliday;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopHolidayRepositoryTest {

    @Autowired
    ShopHolidayRepository shopHolidayRepository;

    @Test
    void findByShopId_returnsAllRowsForShop() {
        shopHolidayRepository.saveAllAndFlush(List.of(
                ShopHoliday.createWeekly(1L, DayOfWeek.SUNDAY),
                ShopHoliday.createCustom(1L, LocalDate.of(2026, 3, 15)),
                ShopHoliday.createCustom(1L, LocalDate.of(2026, 4, 19)),
                ShopHoliday.createWeekly(2L, DayOfWeek.MONDAY)
        ));

        List<ShopHoliday> result = shopHolidayRepository.findByShopId(1L);

        assertThat(result).hasSize(3);
    }
}
