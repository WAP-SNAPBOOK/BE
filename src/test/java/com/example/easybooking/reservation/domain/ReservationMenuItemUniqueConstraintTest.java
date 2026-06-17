package com.example.easybooking.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.reservation.domain.repository.ReservationMenuItemRepository;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ReservationMenuItemUniqueConstraintTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationMenuItemRepository reservationMenuItemRepository;

    @Test
    void save_throwsException_whenDuplicateReservationIdAndShopMenuId() {
        Reservation saved = reservationRepository.save(Reservation.createReservation(
                1L, 1L, 2L,
                LocalDate.of(2026, 2, 11), LocalTime.of(14, 0), List.of()));

        reservationMenuItemRepository.saveAndFlush(
                ReservationMenuItem.create(saved.getId(), 10L, "젤네일", "손관리", null, 0));

        assertThatThrownBy(() -> reservationMenuItemRepository.saveAndFlush(
                ReservationMenuItem.create(saved.getId(), 10L, "젤네일(복사)", "손관리", null, 1)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_allowsSameMenuInDifferentReservations() {
        Reservation r1 = reservationRepository.save(Reservation.createReservation(
                1L, 1L, 2L,
                LocalDate.of(2026, 2, 11), LocalTime.of(14, 0), List.of()));
        Reservation r2 = reservationRepository.save(Reservation.createReservation(
                1L, 1L, 3L,
                LocalDate.of(2026, 2, 11), LocalTime.of(15, 0), List.of()));

        reservationMenuItemRepository.saveAndFlush(
                ReservationMenuItem.create(r1.getId(), 10L, "젤네일", "손관리", null, 0));
        reservationMenuItemRepository.saveAndFlush(
                ReservationMenuItem.create(r2.getId(), 10L, "젤네일", "손관리", null, 0));
        // 다른 예약에 동일 메뉴 -> 허용
    }
}
