package com.example.easybooking.availability.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Table(name = "shop_holidays")
public class ShopHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false, length = 20)
    private HolidayType holidayType;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "week_of_month")
    private Integer weekOfMonth;

    @Column(name = "reference_date")
    private LocalDate referenceDate;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    public static ShopHoliday createWeekly(Long shopId, DayOfWeek dayOfWeek) {
        ShopHoliday shopHoliday = new ShopHoliday();
        shopHoliday.shopId = shopId;
        shopHoliday.holidayType = HolidayType.WEEKLY;
        shopHoliday.dayOfWeek = dayOfWeek;
        return shopHoliday;
    }

    public static ShopHoliday createBiweekly(Long shopId, DayOfWeek dayOfWeek, LocalDate referenceDate) {
        ShopHoliday shopHoliday = new ShopHoliday();
        shopHoliday.shopId = shopId;
        shopHoliday.holidayType = HolidayType.BIWEEKLY;
        shopHoliday.dayOfWeek = dayOfWeek;
        shopHoliday.referenceDate = referenceDate;
        return shopHoliday;
    }

    public static ShopHoliday createMonthly(Long shopId, int weekOfMonth, DayOfWeek dayOfWeek) {
        ShopHoliday shopHoliday = new ShopHoliday();
        shopHoliday.shopId = shopId;
        shopHoliday.holidayType = HolidayType.MONTHLY;
        shopHoliday.weekOfMonth = weekOfMonth;
        shopHoliday.dayOfWeek = dayOfWeek;
        return shopHoliday;
    }

    public static ShopHoliday createCustom(Long shopId, LocalDate specificDate) {
        ShopHoliday shopHoliday = new ShopHoliday();
        shopHoliday.shopId = shopId;
        shopHoliday.holidayType = HolidayType.CUSTOM;
        shopHoliday.specificDate = specificDate;
        return shopHoliday;
    }

    public Long getId() {
        return id;
    }

    public Long getShopId() {
        return shopId;
    }

    public HolidayType getHolidayType() {
        return holidayType;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public Integer getWeekOfMonth() {
        return weekOfMonth;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }
}
