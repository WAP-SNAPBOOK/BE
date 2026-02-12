package com.example.easybooking.availability.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "public_holidays")
public class PublicHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_date", nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(name = "name", length = 100)
    private String name;

    public static PublicHoliday create(LocalDate holidayDate, String name) {
        PublicHoliday publicHoliday = new PublicHoliday();
        publicHoliday.holidayDate = holidayDate;
        publicHoliday.name = name;
        return publicHoliday;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public String getName() {
        return name;
    }
}
