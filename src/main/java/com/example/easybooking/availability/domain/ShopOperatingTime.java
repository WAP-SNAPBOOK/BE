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
import java.time.LocalTime;

@Entity
@Table(name = "shop_operating_times")
public class ShopOperatingTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public static ShopOperatingTime create(Long shopId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        validateRange(startTime, endTime);

        ShopOperatingTime shopOperatingTime = new ShopOperatingTime();
        shopOperatingTime.shopId = shopId;
        shopOperatingTime.dayOfWeek = dayOfWeek;
        shopOperatingTime.startTime = startTime;
        shopOperatingTime.endTime = endTime;
        shopOperatingTime.sortOrder = 0;
        return shopOperatingTime;
    }

    private static void validateRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getShopId() {
        return shopId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
