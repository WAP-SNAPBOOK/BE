package com.example.easybooking.availability.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shop_settings")
public class ShopSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false, unique = true)
    private Long shopId;

    @Column(name = "interval_minutes", nullable = false)
    private int intervalMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 30)
    private ScheduleType scheduleType;

    @Column(name = "booking_window_days", nullable = false)
    private int bookingWindowDays;

    @Column(name = "min_booking_lead_minutes", nullable = false)
    private int minBookingLeadMinutes;

    @Column(name = "public_holiday_off", nullable = false)
    private boolean publicHolidayOff;

    public static ShopSettings createDefault(Long shopId) {
        ShopSettings settings = new ShopSettings();
        settings.shopId = shopId;
        settings.intervalMinutes = 30;
        settings.scheduleType = ScheduleType.DAILY;
        settings.bookingWindowDays = 30;
        settings.minBookingLeadMinutes = 60;
        settings.publicHolidayOff = false;
        return settings;
    }

    public void updateInterval(int intervalMinutes) {
        if (intervalMinutes <= 0) {
            throw new IllegalArgumentException("intervalMinutes must be greater than 0");
        }
        this.intervalMinutes = intervalMinutes;
    }

    public void updateScheduleType(ScheduleType scheduleType) {
        if (scheduleType == null) {
            throw new IllegalArgumentException("scheduleType must not be null");
        }
        this.scheduleType = scheduleType;
    }

    public Long getId() {
        return id;
    }

    public Long getShopId() {
        return shopId;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public int getBookingWindowDays() {
        return bookingWindowDays;
    }

    public int getMinBookingLeadMinutes() {
        return minBookingLeadMinutes;
    }

    public boolean isPublicHolidayOff() {
        return publicHolidayOff;
    }
}
