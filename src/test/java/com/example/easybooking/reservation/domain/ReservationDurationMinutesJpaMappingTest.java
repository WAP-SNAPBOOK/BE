package com.example.easybooking.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ReservationDurationMinutesJpaMappingTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EntityManager em;

    @Test
    void durationMinutes_isMappedToDurationMinutesColumn() {
        Reservation reservation = Reservation.createReservation(
                1L,
                1L,
                2L,
                LocalDate.of(2026, 2, 5),
                LocalTime.of(14, 0), List.of()
        );
        reservation.setDurationMinutes(60);

        Reservation saved = reservationRepository.save(reservation);
        em.flush();
        em.clear();

        Reservation found = em.find(Reservation.class, saved.getId());
        assertThat(found.getDurationMinutes()).isEqualTo(60);
    }
}


