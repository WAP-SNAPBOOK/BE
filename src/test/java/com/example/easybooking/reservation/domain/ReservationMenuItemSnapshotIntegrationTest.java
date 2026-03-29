package com.example.easybooking.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.reservation.domain.repository.ReservationMenuItemRepository;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ReservationMenuItemSnapshotIntegrationTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationMenuItemRepository menuItemRepository;

    @Autowired
    ShopMenuRepository shopMenuRepository;

    @Test
    void snapshot_isPreserved_afterOriginalMenuNameChanges() {
        // given: 메뉴 생성
        ShopMenu menu = shopMenuRepository.saveAndFlush(
                ShopMenu.create(1L, "젤네일", "기본 젤네일", true, 0));

        // 예약 + 메뉴 선택 (스냅샷 = "젤네일")
        Reservation reservation = reservationRepository.save(Reservation.createReservation(
                1L, 1L, 2L,
                LocalDate.of(2026, 2, 11), LocalTime.of(14, 0),
                "{}", List.of()));
        ReservationMenuItem saved = menuItemRepository.saveAndFlush(
                ReservationMenuItem.create(reservation.getId(), menu.getId(), menu.getName(), null, 0));

        // when: 원본 메뉴 이름 변경
        menu.update("젤아트", null, null);
        shopMenuRepository.saveAndFlush(menu);

        // then: 스냅샷은 원래 이름 유지
        ReservationMenuItem found = menuItemRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getMenuNameSnapshot()).isEqualTo("젤네일");
    }
}
