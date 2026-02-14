package com.example.easybooking.availability.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(
        name = "staff_operating_times",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_staff_operating_times_staff_day",
                columnNames = {"staff_id", "day_of_week"}
        )
)
public class StaffOperatingTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "is_off", nullable = false)
    private boolean isOff;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    public static StaffOperatingTime create(Long staffId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime must be before or equal to endTime");
        }

        StaffOperatingTime staffOperatingTime = new StaffOperatingTime();
        staffOperatingTime.staffId = staffId;
        staffOperatingTime.dayOfWeek = dayOfWeek;
        staffOperatingTime.isOff = false;
        staffOperatingTime.startTime = startTime;
        staffOperatingTime.endTime = endTime;
        return staffOperatingTime;
    }

    public static StaffOperatingTime createOff(Long staffId, DayOfWeek dayOfWeek) {
        StaffOperatingTime staffOperatingTime = new StaffOperatingTime();
        staffOperatingTime.staffId = staffId;
        staffOperatingTime.dayOfWeek = dayOfWeek;
        staffOperatingTime.isOff = true;
        staffOperatingTime.startTime = null;
        staffOperatingTime.endTime = null;
        return staffOperatingTime;
    }

    public Long getId() {
        return id;
    }

    public Long getStaffId() {
        return staffId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public boolean isOff() {
        return isOff;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
