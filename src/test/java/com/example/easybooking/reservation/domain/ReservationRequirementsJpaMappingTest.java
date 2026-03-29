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
class ReservationRequirementsJpaMappingTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EntityManager em;

    @Test
    void requirements_isMappedToRequirementsColumn() {
        Reservation reservation = Reservation.createReservation(
                1L,
                1L,
                2L,
                LocalDate.of(2026, 3, 26),
                LocalTime.of(10, 0),
                List.of()
        );
        reservation.setRequirements("짧게 정리해 주세요");

        Reservation saved = reservationRepository.save(reservation);
        em.flush();
        em.clear();

        Reservation found = em.find(Reservation.class, saved.getId());
        assertThat(found.getRequirements()).isEqualTo("짧게 정리해 주세요");
    }
}
