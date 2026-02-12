package com.example.easybooking.availability.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.StaffOperatingTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StaffOperatingTimeRepositoryTest {

    @Autowired
    StaffOperatingTimeRepository staffOperatingTimeRepository;

    @Test
    void findByStaffIdAndDayOfWeek_returnsOptionalWhenExists() {
        staffOperatingTimeRepository.saveAndFlush(
                StaffOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(17, 0))
        );

        Optional<StaffOperatingTime> found = staffOperatingTimeRepository.findByStaffIdAndDayOfWeek(1L, DayOfWeek.MONDAY);

        assertThat(found).isPresent();
    }

    @Test
    void findByStaffIdAndDayOfWeek_returnsEmptyOptionalWhenNotExists() {
        staffOperatingTimeRepository.saveAndFlush(
                StaffOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(17, 0))
        );

        Optional<StaffOperatingTime> found = staffOperatingTimeRepository.findByStaffIdAndDayOfWeek(1L, DayOfWeek.TUESDAY);

        assertThat(found).isEmpty();
    }
}
