package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import java.time.DayOfWeek;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffOperatingTimeReader {

    private final StaffOperatingTimeRepository staffOperatingTimeRepository;

    public Optional<StaffOperatingTime> findByStaffIdAndDayOfWeek(Long staffId, DayOfWeek dayOfWeek) {
        return staffOperatingTimeRepository.findByStaffIdAndDayOfWeek(staffId, dayOfWeek);
    }
}
