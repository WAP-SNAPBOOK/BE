package com.example.easybooking.availability;

import com.example.easybooking.availability.domain.ShopOperatingTime;
import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.exception.StaffIdNotFoundException;
import com.example.easybooking.staff.repository.StaffRepository;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperatingTimeResolver {

    private final StaffRepository staffRepository;
    private final ShopOperatingTimeRepository shopOperatingTimeRepository;
    private final StaffOperatingTimeRepository staffOperatingTimeRepository;

    public List<ShopOperatingTime> resolve(Long staffId, DayOfWeek dayOfWeek) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(StaffIdNotFoundException::new);

        Optional<StaffOperatingTime> override = staffOperatingTimeRepository
                .findByStaffIdAndDayOfWeek(staffId, dayOfWeek);

        if (override.isPresent()) {
            StaffOperatingTime staffOperatingTime = override.get();
            if (staffOperatingTime.isOff()) {
                return List.of();
            }
            return List.of(ShopOperatingTime.create(
                    staff.getShopId(),
                    dayOfWeek,
                    staffOperatingTime.getStartTime(),
                    staffOperatingTime.getEndTime()
            ));
        }

        return shopOperatingTimeRepository.findByShopIdAndDayOfWeek(staff.getShopId(), dayOfWeek)
                .stream()
                .sorted(Comparator.comparing(ShopOperatingTime::getStartTime))
                .toList();
    }
}
