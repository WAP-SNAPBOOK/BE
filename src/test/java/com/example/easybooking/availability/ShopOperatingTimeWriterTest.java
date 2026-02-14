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
class ShopOperatingTimeWriterTest {

    @Autowired
    ShopOperatingTimeRepository shopOperatingTimeRepository;

    @Test
    void replaceAll_replacesExistingRowsWithNewRows() {
        ShopOperatingTimeWriter writer = new ShopOperatingTimeWriter(shopOperatingTimeRepository);

        shopOperatingTimeRepository.saveAllAndFlush(List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)),
                ShopOperatingTime.create(2L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0))
        ));

        List<ShopOperatingTime> replacement = List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(19, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(19, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(19, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(19, 0))
        );

        writer.replaceAll(1L, replacement);

        assertThat(shopOperatingTimeRepository.findByShopId(1L)).hasSize(5);
        assertThat(shopOperatingTimeRepository.findByShopId(2L)).hasSize(1);
    }
}
