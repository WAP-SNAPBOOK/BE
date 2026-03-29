package com.example.easybooking.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ReservationCreateStartAtDualWriteRedTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EntityManager em;

    @Test
    void createReservation_setsStartAt_asDatePlusTime() {
        LocalDate date = LocalDate.of(2026, 2, 5);
        LocalTime time = LocalTime.of(14, 0);
        LocalDateTime expectedStartAt = LocalDateTime.of(date, time);

        Reservation reservation = Reservation.createReservation(
                1L,
                1L,
                2L,
                date,
                time,
                "{\"form\":{}}",
                List.of()
        );

        Reservation saved = reservationRepository.save(reservation);
        em.flush();
        em.clear();

        Reservation found = em.find(Reservation.class, saved.getId());
        assertThat(found.getStartAt()).isEqualTo(expectedStartAt);
    }
}

