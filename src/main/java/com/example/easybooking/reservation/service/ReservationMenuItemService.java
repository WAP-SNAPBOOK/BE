package com.example.easybooking.reservation.service;

import com.example.easybooking.reservation.ReservationMenuItemWriter;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.domain.ShopMenu;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationMenuItemService {

    private final ShopMenuReader shopMenuReader;
    private final ReservationMenuItemWriter reservationMenuItemWriter;

    public List<ReservationMenuItem> saveMenuItems(Long reservationId, Long shopId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            throw new IllegalArgumentException("메뉴를 최소 1개 이상 선택해야 합니다.");
        }

        AtomicInteger sortOrder = new AtomicInteger(0);

        List<ReservationMenuItem> items = menuIds.stream()
                .map(menuId -> {
                    ShopMenu menu = shopMenuReader.getById(menuId);

                    if (!menu.getShopId().equals(shopId)) {
                        throw new IllegalArgumentException(
                                "메뉴(id=" + menuId + ")가 해당 매장(shopId=" + shopId + ")에 속하지 않습니다.");
                    }

                    if (!menu.getIsActive()) {
                        throw new IllegalArgumentException(
                                "비활성 메뉴(id=" + menuId + ")는 선택할 수 없습니다.");
                    }

                    return ReservationMenuItem.create(
                            reservationId,
                            menuId,
                            menu.getName(),
                            null,
                            sortOrder.getAndIncrement()
                    );
                })
                .toList();

        return reservationMenuItemWriter.saveAll(items);
    }
}
