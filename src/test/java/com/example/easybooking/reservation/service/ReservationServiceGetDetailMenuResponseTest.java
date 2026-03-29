package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.easybooking.reservation.ReservationMenuInputValueReader;
import com.example.easybooking.reservation.ReservationMenuItemReader;
import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationTimeBlockWriter;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.TimeBlockGenerator;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationMenuInputValue;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.dto.ReservationDetailResponse;
import com.example.easybooking.reservation.dto.ReservationMenuItemResponse;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReservationServiceGetDetailMenuResponseTest {

    @Mock ReservationWriter reservationWriter;
    @Mock ReservationReader reservationReader;
    @Mock UserReader userReader;
    @Mock ShopReader shopReader;
    @Mock StaffReader staffReader;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock TimeBlockGenerator timeBlockGenerator;
    @Mock ReservationTimeBlockWriter reservationTimeBlockWriter;
    @Mock ReservationMenuItemService reservationMenuItemService;
    @Mock ReservationMenuInputValueService reservationMenuInputValueService;
    @Mock ReservationMenuItemReader menuItemReader;
    @Mock ReservationMenuInputValueReader inputValueReader;

    ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationWriter, reservationReader, userReader, shopReader, staffReader,
                new ObjectMapper(), eventPublisher, timeBlockGenerator,
                reservationTimeBlockWriter,
                reservationMenuItemService, reservationMenuInputValueService,
                menuItemReader, inputValueReader
        );
    }

    // --- 공통 헬퍼 ---

    private Reservation mockReservation(Long reservationId, Long customerId, Long ownerUserId, Long shopId) {
        Reservation r = mock(Reservation.class);
        when(r.getId()).thenReturn(reservationId);
        when(r.getCustomerId()).thenReturn(customerId);
        when(r.getOwnerUserId()).thenReturn(ownerUserId);
        when(r.getShopId()).thenReturn(shopId);
        when(r.getFormDataJson()).thenReturn(
                "{\"part\":\"손\",\"removal\":\"예\",\"requests\":\"요청\",\"extend\":\"0\",\"wrapping\":\"0\"}");
        when(r.getDesignImageURLs()).thenReturn(List.of());
        when(r.getDate()).thenReturn(LocalDate.of(2026, 2, 11));
        when(r.getTime()).thenReturn(LocalTime.of(14, 0));
        when(r.getStatus()).thenReturn(Reservation.Status.PENDING);
        return r;
    }

    private void setupUserAndShop(Long customerId, Long shopId) {
        User customer = mock(User.class);
        when(customer.getName()).thenReturn("고객");
        when(customer.getPhoneNumber()).thenReturn("01012345678");
        when(userReader.read(customerId)).thenReturn(customer);

        Shop shop = mock(Shop.class);
        when(shop.getBusinessName()).thenReturn("테스트샵");
        when(shopReader.read(shopId)).thenReturn(shop);
    }

    private ReservationMenuItem mockMenuItem(Long itemId,
                                             Long shopMenuId, String name, Long price, int sortOrder) {
        ReservationMenuItem item = mock(ReservationMenuItem.class);
        when(item.getId()).thenReturn(itemId);
        when(item.getShopMenuId()).thenReturn(shopMenuId);
        when(item.getMenuNameSnapshot()).thenReturn(name);
        when(item.getPriceSnapshot()).thenReturn(price);
        when(item.getSortOrder()).thenReturn(sortOrder);
        return item;
    }

    private ReservationMenuInputValue mockInputValue(Long menuItemId,
                                                     String label, String type,
                                                     BigDecimal number, String text) {
        ReservationMenuInputValue iv = mock(ReservationMenuInputValue.class);
        when(iv.getReservationMenuItemId()).thenReturn(menuItemId);
        when(iv.getFieldLabelSnapshot()).thenReturn(label);
        when(iv.getInputTypeSnapshot()).thenReturn(type);
        when(iv.getValueNumber()).thenReturn(number);
        when(iv.getValueText()).thenReturn(text);
        return iv;
    }

    // --- 5-A-1 ---

    @Test
    @DisplayName("5-A-1: 예약 상세 조회 시 선택된 메뉴 목록 + 입력값이 포함된다")
    void getReservationDetail_containsMenusWithInputValues() {
        // given
        Long reservationId = 1L;
        Long customerId = 10L;
        Long shopId = 100L;

        Reservation reservation = mockReservation(reservationId, customerId, customerId, shopId);
        when(reservationReader.getById(reservationId)).thenReturn(reservation);
        setupUserAndShop(customerId, shopId);

        ReservationMenuItem item1 = mockMenuItem(101L, 50L, "젤네일", 50000L, 0);
        ReservationMenuItem item2 = mockMenuItem(102L, 60L, "아트", 30000L, 1);
        when(menuItemReader.findByReservationId(reservationId)).thenReturn(List.of(item1, item2));

        ReservationMenuInputValue iv1 = mockInputValue(101L, "길이", "NUMBER",
                new BigDecimal("5"), null);
        when(inputValueReader.findByReservationMenuItemIds(List.of(101L, 102L)))
                .thenReturn(List.of(iv1));

        // when
        ReservationDetailResponse result = reservationService.getReservationDetail(reservationId, customerId);

        // then
        assertThat(result.getMenus()).hasSize(2);

        ReservationMenuItemResponse menu1 = result.getMenus().get(0);
        assertThat(menu1.getMenuNameSnapshot()).isEqualTo("젤네일");
        assertThat(menu1.getShopMenuId()).isEqualTo(50L);
        assertThat(menu1.getPriceSnapshot()).isEqualTo(50000L);
        assertThat(menu1.getInputValues()).hasSize(1);
        assertThat(menu1.getInputValues().get(0).getFieldLabelSnapshot()).isEqualTo("길이");
        assertThat(menu1.getInputValues().get(0).getValueNumber()).isEqualByComparingTo(new BigDecimal("5"));

        ReservationMenuItemResponse menu2 = result.getMenus().get(1);
        assertThat(menu2.getMenuNameSnapshot()).isEqualTo("아트");
        assertThat(menu2.getInputValues()).isEmpty();
    }

    // --- 5-A-2 ---

    @Test
    @DisplayName("5-A-2: reservation_menu_items 데이터가 없으면 formDataJson fallback")
    void getReservationDetail_fallsBackToFormDataJson_whenNoMenuItems() {
        // given
        Long reservationId = 2L;
        Long customerId = 20L;
        Long shopId = 200L;

        Reservation reservation = mockReservation(reservationId, customerId, customerId, shopId);
        when(reservationReader.getById(reservationId)).thenReturn(reservation);
        setupUserAndShop(customerId, shopId);

        when(menuItemReader.findByReservationId(reservationId)).thenReturn(List.of());

        // when
        ReservationDetailResponse result = reservationService.getReservationDetail(reservationId, customerId);

        // then: formDataJson 기반 필드가 정상 반환
        assertThat(result.getPart()).isEqualTo("손");
        assertThat(result.getRemoval()).isEqualTo("예");
        assertThat(result.getRequests()).isEqualTo("요청");

        // then: menus는 빈 리스트
        assertThat(result.getMenus()).isEmpty();
    }

    // --- 5-A-3 ---

    @Test
    @DisplayName("5-A-3: 메뉴별 입력값이 해당 메뉴 하위로 중첩 반환된다")
    void getReservationDetail_nestsInputValuesUnderCorrectMenu() {
        // given
        Long reservationId = 3L;
        Long customerId = 30L;
        Long shopId = 300L;

        Reservation reservation = mockReservation(reservationId, customerId, customerId, shopId);
        when(reservationReader.getById(reservationId)).thenReturn(reservation);
        setupUserAndShop(customerId, shopId);

        ReservationMenuItem menuA = mockMenuItem(201L, 70L, "메뉴A", 40000L, 0);
        ReservationMenuItem menuB = mockMenuItem(202L, 80L, "메뉴B", 20000L, 1);
        when(menuItemReader.findByReservationId(reservationId)).thenReturn(List.of(menuA, menuB));

        ReservationMenuInputValue ivA1 = mockInputValue(201L, "길이", "NUMBER",
                new BigDecimal("3"), null);
        ReservationMenuInputValue ivA2 = mockInputValue(201L, "요청사항", "TEXT",
                null, "짧게");
        when(inputValueReader.findByReservationMenuItemIds(List.of(201L, 202L)))
                .thenReturn(List.of(ivA1, ivA2));

        // when
        ReservationDetailResponse result = reservationService.getReservationDetail(reservationId, customerId);

        // then: 메뉴 A -> inputValues 2개
        ReservationMenuItemResponse resultMenuA = result.getMenus().get(0);
        assertThat(resultMenuA.getMenuNameSnapshot()).isEqualTo("메뉴A");
        assertThat(resultMenuA.getInputValues()).hasSize(2);
        assertThat(resultMenuA.getInputValues().get(0).getFieldLabelSnapshot()).isEqualTo("길이");
        assertThat(resultMenuA.getInputValues().get(1).getFieldLabelSnapshot()).isEqualTo("요청사항");
        assertThat(resultMenuA.getInputValues().get(1).getValueText()).isEqualTo("짧게");

        // then: 메뉴 B -> inputValues 0개
        ReservationMenuItemResponse resultMenuB = result.getMenus().get(1);
        assertThat(resultMenuB.getMenuNameSnapshot()).isEqualTo("메뉴B");
        assertThat(resultMenuB.getInputValues()).isEmpty();
    }
}
