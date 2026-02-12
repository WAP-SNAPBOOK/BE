package com.example.easybooking.availability.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class StaffOperatingTimeTest {

    @Test
    void create_returnsOverrideWithTimeRange() {
        StaffOperatingTime staffOperatingTime = StaffOperatingTime.create(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(17, 0)
        );

        assertThat(staffOperatingTime.getStaffId()).isEqualTo(1L);
        assertThat(staffOperatingTime.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(staffOperatingTime.isOff()).isFalse();
        assertThat(staffOperatingTime.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(staffOperatingTime.getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    void createOff_returnsDayOffWithNullTimeRange() {
        StaffOperatingTime staffOperatingTime = StaffOperatingTime.createOff(1L, DayOfWeek.WEDNESDAY);

        assertThat(staffOperatingTime.getStaffId()).isEqualTo(1L);
        assertThat(staffOperatingTime.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(staffOperatingTime.isOff()).isTrue();
        assertThat(staffOperatingTime.getStartTime()).isNull();
        assertThat(staffOperatingTime.getEndTime()).isNull();
    }
}
