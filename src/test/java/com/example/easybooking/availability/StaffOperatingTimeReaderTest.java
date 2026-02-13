package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StaffOperatingTimeReaderTest {

    @Autowired
    StaffOperatingTimeRepository staffOperatingTimeRepository;

    @Test
    void findByStaffIdAndDayOfWeek_returnsOverride_whenExists() {
        StaffOperatingTimeReader reader = new StaffOperatingTimeReader(staffOperatingTimeRepository);

        staffOperatingTimeRepository.saveAndFlush(
                StaffOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(17, 0))
        );

        Optional<StaffOperatingTime> found = reader.findByStaffIdAndDayOfWeek(1L, DayOfWeek.MONDAY);

        assertThat(found).isPresent();
        assertThat(found.get().getStaffId()).isEqualTo(1L);
        assertThat(found.get().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void findByStaffIdAndDayOfWeek_returnsEmpty_whenNotExists() {
        StaffOperatingTimeReader reader = new StaffOperatingTimeReader(staffOperatingTimeRepository);

        staffOperatingTimeRepository.saveAndFlush(
                StaffOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(17, 0))
        );

        Optional<StaffOperatingTime> found = reader.findByStaffIdAndDayOfWeek(1L, DayOfWeek.TUESDAY);

        assertThat(found).isEmpty();
    }
}
