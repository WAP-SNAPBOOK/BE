package com.example.easybooking.availability.repository;

import com.example.easybooking.availability.domain.StaffOperatingTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffOperatingTimeRepository extends JpaRepository<StaffOperatingTime, Long> {
    Optional<StaffOperatingTime> findByStaffIdAndDayOfWeek(Long staffId, DayOfWeek dayOfWeek);

    List<StaffOperatingTime> findByStaffId(Long staffId);

    void deleteByStaffId(Long staffId);
}
