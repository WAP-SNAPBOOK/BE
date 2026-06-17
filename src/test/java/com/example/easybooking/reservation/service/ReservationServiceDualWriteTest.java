package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.easybooking.reservation.ReservationReader;
import com.example.easybooking.reservation.ReservationTimeBlockWriter;
import com.example.easybooking.reservation.ReservationWriter;
import com.example.easybooking.reservation.TimeBlockGenerator;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.dto.MenuInputValueRequest;
import com.example.easybooking.reservation.dto.MenuSelectionRequest;
import com.example.easybooking.reservation.dto.ReservationCreateRequest;
import com.example.easybooking.reservation.dto.ReservationResponse;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.staff.StaffReader;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReservationServiceDualWriteTest {

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
    @Mock com.example.easybooking.reservation.ReservationMenuItemReader menuItemReader;
    @Mock com.example.easybooking.reservation.ReservationMenuInputValueReader inputValueReader;

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

    private ReservationCreateRequest buildRequest(List<MenuSelectionRequest> menuSelections) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setShopId(1L);
        request.setStaffId(10L);
        request.setDate(LocalDate.of(2026, 2, 11));
        request.setTime(LocalTime.of(14, 0));
        request.setMenuSelections(menuSelections);
        return request;
    }

    private void setupCommonMocks() {
        Shop shop = mock(Shop.class);
        when(shop.getOwnerId()).thenReturn(100L);
        when(shopReader.read(1L)).thenReturn(shop);

        Staff staff = mock(Staff.class);
        when(staff.getShopId()).thenReturn(1L);
        when(staffReader.read(10L)).thenReturn(staff);

        User customer = mock(User.class);
        when(customer.getName()).thenReturn("고객");
        when(userReader.read(200L)).thenReturn(customer);

        when(reservationWriter.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            try {
                var idField = Reservation.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(r, 999L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return r;
        });
    }

    // 4-E-1: reservations + 새 테이블 동시 저장
    @Test
    void createReservation_savesFormDataJsonAndMenuItems() {
        setupCommonMocks();

        ReservationMenuItem item1 = ReservationMenuItem.create(999L, 50L, "젤네일", "손관리", null, 0);
        ReservationMenuItem item2 = ReservationMenuItem.create(999L, 60L, "아트", null, null, 1);
        when(reservationMenuItemService.saveMenuItems(eq(999L), eq(1L), anyList()))
                .thenReturn(List.of(item1, item2));

        ReservationCreateRequest request = buildRequest(List.of(
                new MenuSelectionRequest(50L, 1001L, List.of()),
                new MenuSelectionRequest(60L, 1002L, List.of(
                        new MenuInputValueRequest(100L, new BigDecimal("5"), null)))
        ));

        ReservationResponse response = reservationService.createReservation(request, 200L);

        ArgumentCaptor<List<MenuSelectionRequest>> selectionsCaptor = ArgumentCaptor.forClass(List.class);
        assertThat(response).isNotNull();
        verify(reservationMenuItemService).saveMenuItems(eq(999L), eq(1L), selectionsCaptor.capture());
        assertThat(selectionsCaptor.getValue())
                .extracting(MenuSelectionRequest::getMenuId, MenuSelectionRequest::getTagId)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(50L, 1001L),
                        org.assertj.core.groups.Tuple.tuple(60L, 1002L)
                );
        verify(reservationMenuInputValueService).saveInputValues(
                eq(item2.getId()), eq(60L), anyList());
    }

    // 4-E-1 보완: menuSelections가 null이면 레거시 모드
    @Test
    void createReservation_skipsMenuSave_whenMenuSelectionsNull() {
        setupCommonMocks();

        ReservationCreateRequest request = buildRequest(null);
        reservationService.createReservation(request, 200L);

        verify(reservationMenuItemService, never()).saveMenuItems(anyLong(), anyLong(), anyList());
    }

    // 4-E-2: 메뉴 저장 실패 시 예외 전파 (트랜잭션 롤백)
    @Test
    void createReservation_propagatesException_whenMenuSaveFails() {
        setupCommonMocks();

        when(reservationMenuItemService.saveMenuItems(anyLong(), anyLong(), anyList()))
                .thenThrow(new IllegalArgumentException("메뉴 저장 실패"));

        ReservationCreateRequest request = buildRequest(List.of(
                new MenuSelectionRequest(50L, 1001L, List.of())));

        assertThatThrownBy(() -> reservationService.createReservation(request, 200L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메뉴 저장 실패");
    }
}
