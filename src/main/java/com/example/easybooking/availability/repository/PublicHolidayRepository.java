package com.example.easybooking.availability.repository;

import com.example.easybooking.availability.domain.PublicHoliday;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {
    boolean existsByHolidayDate(LocalDate holidayDate);

    List<PublicHoliday> findByHolidayDateBetween(LocalDate startDate, LocalDate endDate);
}
