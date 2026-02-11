package com.example.easybooking.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.reservation.domain.repository.ReservationMenuItemRepository;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ReservationMenuInputValueJpaMappingTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationMenuItemRepository menuItemRepository;

    @Autowired
    EntityManager em;

    @Test
    void reservationMenuInputValue_isMappedToReservationMenuInputValuesTable() {
        Reservation reservation = reservationRepository.save(Reservation.createReservation(
                1L, 1L, 2L,
                LocalDate.of(2026, 2, 11), LocalTime.of(14, 0),
                "{}", List.of()));

        ReservationMenuItem menuItem = menuItemRepository.saveAndFlush(
                ReservationMenuItem.create(reservation.getId(), 10L, "젤네일", null, 0));

        ReservationMenuInputValue inputValue = ReservationMenuInputValue.create(
                menuItem.getId(),
                100L,
                "갯수",
                "NUMBER",
                new BigDecimal("5"),
                null
        );

        em.persist(inputValue);
        em.flush();
        em.clear();

        ReservationMenuInputValue found = em.find(ReservationMenuInputValue.class, inputValue.getId());
        assertThat(found).isNotNull();
        assertThat(found.getReservationMenuItemId()).isEqualTo(menuItem.getId());
        assertThat(found.getShopMenuInputFieldId()).isEqualTo(100L);
        assertThat(found.getFieldLabelSnapshot()).isEqualTo("갯수");
        assertThat(found.getInputTypeSnapshot()).isEqualTo("NUMBER");
        assertThat(found.getValueNumber()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(found.getValueText()).isNull();
    }
}
