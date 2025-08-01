package com.example.easybooking.slot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByShopIdAndIsBookedFalseAndStartDateTimeBetween(
            Long shopId,
            LocalDateTime start,
            LocalDateTime end
    );
    @Query("SELECT DISTINCT CAST(s.startDateTime AS LocalDate) " +
            "FROM Slot s " +
            "WHERE s.shop.id = :shopId " +
            "AND s.isBooked = false " +
            "AND s.startDateTime BETWEEN :start AND :end " +
            "ORDER BY CAST(s.startDateTime AS LocalDate) ASC")
    List<LocalDate> findDistinctAvailableDates(
            @Param("shopId") Long shopId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}