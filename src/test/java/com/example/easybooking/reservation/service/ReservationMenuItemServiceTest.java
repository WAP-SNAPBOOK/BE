package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.easybooking.reservation.ReservationMenuItemWriter;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.dto.MenuSelectionRequest;
import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.dto.response.MenuTagRow;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.ShopTagRepository;
import com.example.easybooking.shop.repository.TagRepository;
import java.util.List;
import java.util.Optional;
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

    @Mock
    ShopMenuTagRepository shopMenuTagRepository;

    @Mock
    ShopTagRepository shopTagRepository;

    @Mock
    TagRepository tagRepository;

    @InjectMocks
    ReservationMenuItemService reservationMenuItemService;

    @Test
    void saveMenuItems_persistsAllMenuItemsWithSnapshots() {
        // given
        ShopMenu menu1 = ShopMenu.create(1L, "젤네일", "기본 젤네일", 50000L, true, 0);
        ShopMenu menu2 = ShopMenu.create(1L, "아트", "아트 추가", 30000L, true, 1);
        ShopTag tag1 = mock(ShopTag.class);
        ShopTag tag2 = mock(ShopTag.class);
        when(tag1.getName()).thenReturn("손관리");
        when(tag2.getName()).thenReturn("아트");

        when(shopMenuReader.getById(10L)).thenReturn(menu1);
        when(shopMenuReader.getById(20L)).thenReturn(menu2);
        when(shopMenuTagRepository.findByShopMenuIdAndAnyTagId(10L, 1001L))
                .thenReturn(Optional.of(ShopMenuTag.createResolved(10L, 1L, 1001L)));
        when(shopMenuTagRepository.findByShopMenuIdAndAnyTagId(20L, 1002L))
                .thenReturn(Optional.of(ShopMenuTag.createResolved(20L, 2L, 1002L)));
        when(shopTagRepository.findByIdAndShopId(1001L, 1L)).thenReturn(Optional.of(tag1));
        when(shopTagRepository.findByIdAndShopId(1002L, 1L)).thenReturn(Optional.of(tag2));
        when(reservationMenuItemWriter.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // when
        List<ReservationMenuItem> result = reservationMenuItemService.saveMenuItems(
                100L, 1L, List.of(
                        new MenuSelectionRequest(10L, 1001L, List.of()),
                        new MenuSelectionRequest(20L, 1002L, List.of())
                ));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReservationId()).isEqualTo(100L);
        assertThat(result.get(0).getMenuNameSnapshot()).isEqualTo("젤네일");
        assertThat(result.get(0).getTagNameSnapshot()).isEqualTo("손관리");
        assertThat(result.get(0).getPriceSnapshot()).isEqualTo(50000L);
        assertThat(result.get(0).getShopMenuId()).isEqualTo(10L);
        assertThat(result.get(0).getSortOrder()).isEqualTo(0);

        assertThat(result.get(1).getMenuNameSnapshot()).isEqualTo("아트");
        assertThat(result.get(1).getTagNameSnapshot()).isEqualTo("아트");
        assertThat(result.get(1).getPriceSnapshot()).isEqualTo(30000L);
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

        assertThatThrownBy(() -> reservationMenuItemService.saveMenuItems(
                100L,
                1L,
                List.of(new MenuSelectionRequest(10L, 1001L, List.of()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("속하지 않습니다");
    }

    @Test
    void saveMenuItems_throwsException_whenMenuIsInactive() {
        ShopMenu inactiveMenu = ShopMenu.create(1L, "비활성메뉴", null, false, 0);
        when(shopMenuReader.getById(10L)).thenReturn(inactiveMenu);

        assertThatThrownBy(() -> reservationMenuItemService.saveMenuItems(
                100L,
                1L,
                List.of(new MenuSelectionRequest(10L, 1001L, List.of()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비활성");
    }

    @Test
    void saveMenuItems_throwsException_whenTagIdMissing() {
        assertThatThrownBy(() -> reservationMenuItemService.saveMenuItems(
                100L,
                1L,
                List.of(new MenuSelectionRequest(10L, null, List.of()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("태그를 선택");
    }
}
