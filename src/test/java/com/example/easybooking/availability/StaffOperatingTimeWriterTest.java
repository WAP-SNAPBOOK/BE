package com.example.easybooking.availability;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.availability.domain.StaffOperatingTime;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class StaffOperatingTimeWriterTest {

    @Autowired
    StaffOperatingTimeRepository staffOperatingTimeRepository;

    @Test
    void save_persistsStaffOperatingTime() {
        StaffOperatingTimeWriter writer = new StaffOperatingTimeWriter(staffOperatingTimeRepository);

        StaffOperatingTime saved = writer.save(
                StaffOperatingTime.create(1L, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(17, 0))
        );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStaffId()).isEqualTo(1L);
        assertThat(saved.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }
}
