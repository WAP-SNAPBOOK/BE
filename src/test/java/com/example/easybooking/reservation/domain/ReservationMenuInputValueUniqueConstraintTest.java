package com.example.easybooking.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.reservation.domain.repository.ReservationMenuInputValueRepository;
import com.example.easybooking.reservation.domain.repository.ReservationMenuItemRepository;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ReservationMenuInputValueUniqueConstraintTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationMenuItemRepository menuItemRepository;

    @Autowired
    ReservationMenuInputValueRepository inputValueRepository;

    @Test
    void save_throwsException_whenDuplicateMenuItemIdAndFieldId() {
        Reservation reservation = reservationRepository.save(Reservation.createReservation(
                1L, 1L, 2L,
                LocalDate.of(2026, 2, 11), LocalTime.of(14, 0), List.of()));

        ReservationMenuItem menuItem = menuItemRepository.saveAndFlush(
                ReservationMenuItem.create(reservation.getId(), 10L, "젤네일", "손관리", null, 0));

        inputValueRepository.saveAndFlush(ReservationMenuInputValue.create(
                menuItem.getId(), 100L, "갯수", "NUMBER", new BigDecimal("5"), null));

        assertThatThrownBy(() -> inputValueRepository.saveAndFlush(
                ReservationMenuInputValue.create(
                        menuItem.getId(), 100L, "갯수(복사)", "NUMBER", new BigDecimal("3"), null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_allowsSameFieldInDifferentMenuItems() {
        Reservation reservation = reservationRepository.save(Reservation.createReservation(
                1L, 1L, 2L,
                LocalDate.of(2026, 2, 11), LocalTime.of(14, 0), List.of()));

        ReservationMenuItem item1 = menuItemRepository.saveAndFlush(
                ReservationMenuItem.create(reservation.getId(), 10L, "젤네일", "손관리", null, 0));
        ReservationMenuItem item2 = menuItemRepository.saveAndFlush(
                ReservationMenuItem.create(reservation.getId(), 20L, "아트", "아트태그", null, 1));

        inputValueRepository.saveAndFlush(ReservationMenuInputValue.create(
                item1.getId(), 100L, "갯수", "NUMBER", new BigDecimal("5"), null));
        inputValueRepository.saveAndFlush(ReservationMenuInputValue.create(
                item2.getId(), 100L, "갯수", "NUMBER", new BigDecimal("3"), null));
        // 다른 menuItem에 동일 fieldId -> 허용
    }
}
