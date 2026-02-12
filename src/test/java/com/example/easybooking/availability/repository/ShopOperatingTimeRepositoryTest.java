package com.example.easybooking.availability.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopOperatingTimeRepositoryTest {

    @Autowired
    ShopOperatingTimeRepository shopOperatingTimeRepository;

    @Test
    void findByShopId_returnsAllRowsForShop() {
        shopOperatingTimeRepository.saveAllAndFlush(List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(19, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(19, 0)),
                ShopOperatingTime.create(2L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0))
        ));

        List<ShopOperatingTime> result = shopOperatingTimeRepository.findByShopId(1L);

        assertThat(result).hasSize(3);
    }

    @Test
    void findByShopIdAndDayOfWeek_returnsRowsForDay() {
        shopOperatingTimeRepository.saveAllAndFlush(List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(19, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        ));

        List<ShopOperatingTime> result = shopOperatingTimeRepository.findByShopIdAndDayOfWeek(1L, DayOfWeek.MONDAY);

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteByShopId_deletesAllRowsForShop() {
        shopOperatingTimeRepository.saveAllAndFlush(List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.FRIDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)),
                ShopOperatingTime.create(2L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0))
        ));

        shopOperatingTimeRepository.deleteByShopId(1L);

        assertThat(shopOperatingTimeRepository.findByShopId(1L)).isEmpty();
        assertThat(shopOperatingTimeRepository.findByShopId(2L)).hasSize(1);
    }
}
