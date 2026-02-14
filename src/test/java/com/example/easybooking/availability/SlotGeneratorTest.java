package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlotGeneratorTest {

    @Test
    void generate_returnsSlotsForSingleBlockWith30Minutes() {
        SlotGenerator slotGenerator = new SlotGenerator();
        List<ShopOperatingTime> timeBlocks = List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        List<LocalTime> slots = slotGenerator.generate(timeBlocks, 30);

        assertThat(slots).hasSize(19);
        assertThat(slots.get(0)).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(slots.size() - 1)).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    void generate_returnsSlotsForSingleBlockWith60Minutes() {
        SlotGenerator slotGenerator = new SlotGenerator();
        List<ShopOperatingTime> timeBlocks = List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))
        );

        List<LocalTime> slots = slotGenerator.generate(timeBlocks, 60);

        assertThat(slots).hasSize(10);
        assertThat(slots.get(0)).isEqualTo(LocalTime.of(9, 0));
        assertThat(slots.get(slots.size() - 1)).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    void generate_returnsSlotsForMultipleBlocksWith30Minutes() {
        SlotGenerator slotGenerator = new SlotGenerator();
        List<ShopOperatingTime> timeBlocks = List.of(
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(13, 0)),
                ShopOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(19, 0))
        );

        List<LocalTime> slots = slotGenerator.generate(timeBlocks, 30);

        assertThat(slots).hasSize(18);
        assertThat(slots).contains(LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(19, 0));
        assertThat(slots.get(0)).isEqualTo(LocalTime.of(10, 0));
        assertThat(slots.get(slots.size() - 1)).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    void generate_returnsEmpty_whenTimeBlocksEmpty() {
        SlotGenerator slotGenerator = new SlotGenerator();

        List<LocalTime> slots = slotGenerator.generate(List.of(), 30);

        assertThat(slots).isEmpty();
    }
}
