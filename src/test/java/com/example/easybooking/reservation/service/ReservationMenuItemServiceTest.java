package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.example.easybooking.reservation.ReservationMenuItemWriter;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.domain.ShopMenu;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationMenuItemServiceTest {

    @Mock
    ShopMenuReader shopMenuReader;

    @Mock
    ReservationMenuItemWriter reservationMenuItemWriter;

    @InjectMocks
    ReservationMenuItemService reservationMenuItemService;

    @Test
    void saveMenuItems_persistsAllMenuItemsWithSnapshots() {
        // given
        ShopMenu menu1 = ShopMenu.create(1L, "젤네일", "기본 젤네일", true, 0);
        ShopMenu menu2 = ShopMenu.create(1L, "아트", "아트 추가", true, 1);

        when(shopMenuReader.getById(10L)).thenReturn(menu1);
        when(shopMenuReader.getById(20L)).thenReturn(menu2);
        when(reservationMenuItemWriter.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // when
        List<ReservationMenuItem> result = reservationMenuItemService.saveMenuItems(
                100L, 1L, List.of(10L, 20L));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReservationId()).isEqualTo(100L);
        assertThat(result.get(0).getMenuNameSnapshot()).isEqualTo("젤네일");
        assertThat(result.get(0).getShopMenuId()).isEqualTo(10L);
        assertThat(result.get(0).getSortOrder()).isEqualTo(0);

        assertThat(result.get(1).getMenuNameSnapshot()).isEqualTo("아트");
        assertThat(result.get(1).getShopMenuId()).isEqualTo(20L);
        assertThat(result.get(1).getSortOrder()).isEqualTo(1);
    }

    @Test
    void saveMenuItems_throwsException_whenMenuIdsEmpty() {
        assertThatThrownBy(() -> reservationMenuItemService.saveMenuItems(100L, 1L, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveMenuItems_throwsException_whenMenuIdsNull() {
        assertThatThrownBy(() -> reservationMenuItemService.saveMenuItems(100L, 1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveMenuItems_throwsException_whenMenuBelongsToDifferentShop() {
        ShopMenu wrongShopMenu = ShopMenu.create(2L, "다른샵메뉴", null, true, 0);
        when(shopMenuReader.getById(10L)).thenReturn(wrongShopMenu);

        assertThatThrownBy(() -> reservationMenuItemService.saveMenuItems(100L, 1L, List.of(10L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("속하지 않습니다");
    }

    @Test
    void saveMenuItems_throwsException_whenMenuIsInactive() {
        ShopMenu inactiveMenu = ShopMenu.create(1L, "비활성메뉴", null, false, 0);
        when(shopMenuReader.getById(10L)).thenReturn(inactiveMenu);

        assertThatThrownBy(() -> reservationMenuItemService.saveMenuItems(100L, 1L, List.of(10L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비활성");
    }
}
