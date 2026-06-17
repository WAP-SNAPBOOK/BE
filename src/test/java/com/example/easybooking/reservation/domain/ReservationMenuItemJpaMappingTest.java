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
class ReservationMenuItemJpaMappingTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EntityManager em;

    @Test
    void reservationMenuItem_isMappedToReservationMenuItemsTable() {
        Reservation reservation = Reservation.createReservation(
                1L,
                1L,
                2L,
                LocalDate.of(2026, 2, 11),
                LocalTime.of(14, 0), List.of()
        );
        Reservation savedReservation = reservationRepository.save(reservation);

        ReservationMenuItem reservationMenuItem = ReservationMenuItem.create(
                savedReservation.getId(),
                10L,
                "젤네일",
                "손관리",
                null,
                0
        );

        em.persist(reservationMenuItem);
        em.flush();
        em.clear();

        ReservationMenuItem found = em.find(ReservationMenuItem.class, reservationMenuItem.getId());
        assertThat(found).isNotNull();
        assertThat(found.getReservationId()).isEqualTo(savedReservation.getId());
        assertThat(found.getShopMenuId()).isEqualTo(10L);
        assertThat(found.getMenuNameSnapshot()).isEqualTo("젤네일");
        assertThat(found.getTagNameSnapshot()).isEqualTo("손관리");
        assertThat(found.getSortOrder()).isEqualTo(0);
    }
}
