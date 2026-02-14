package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SlotGenerator {

    public List<LocalTime> generate(List<ShopOperatingTime> timeBlocks, int intervalMinutes) {
        if (timeBlocks == null || timeBlocks.isEmpty()) {
            return List.of();
        }
        if (intervalMinutes <= 0) {
            throw new IllegalArgumentException("intervalMinutes must be greater than 0");
        }

        LinkedHashSet<LocalTime> slots = new LinkedHashSet<>();
        List<ShopOperatingTime> sortedBlocks = timeBlocks.stream()
                .sorted(Comparator.comparing(ShopOperatingTime::getStartTime))
                .toList();

        for (ShopOperatingTime block : sortedBlocks) {
            LocalTime current = block.getStartTime();
            while (!current.isAfter(block.getEndTime())) {
                slots.add(current);
                current = current.plusMinutes(intervalMinutes);
            }
        }

        return List.copyOf(slots);
    }
}
