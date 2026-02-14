package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffOperatingTimeWriter {

    private final StaffOperatingTimeRepository staffOperatingTimeRepository;

    public StaffOperatingTime save(StaffOperatingTime staffOperatingTime) {
        return staffOperatingTimeRepository.save(staffOperatingTime);
    }
}
