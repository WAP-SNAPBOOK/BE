package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopOperatingTimeReaderTest {

    @Autowired
    ShopOperatingTimeRepository shopOperatingTimeRepository;

    @Test
    void readByShopIdAndDayOfWeek_returnsRowsForDay() {
        ShopOperatingTimeReader reader = new ShopOperatingTimeReader(shopOperatingTimeRepository);

        shopOperatingTimeRepository.saveAllAndFlush(List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(19, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        ));

        List<ShopOperatingTime> result = reader.readByShopIdAndDayOfWeek(1L, DayOfWeek.MONDAY);

        assertThat(result).hasSize(2);
    }
}
