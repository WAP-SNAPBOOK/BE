package com.example.easybooking.reservation.service;

import com.example.easybooking.reservation.ReservationMenuItemWriter;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.dto.MenuSelectionRequest;
import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.domain.Tag;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.ShopTagRepository;
import com.example.easybooking.shop.repository.TagRepository;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationMenuItemService {

    private final ShopMenuReader shopMenuReader;
    private final ReservationMenuItemWriter reservationMenuItemWriter;
    private final ShopMenuTagRepository shopMenuTagRepository;
    private final ShopTagRepository shopTagRepository;
    private final TagRepository tagRepository;

    public List<ReservationMenuItem> saveMenuItems(
            Long reservationId,
            Long shopId,
            List<MenuSelectionRequest> menuSelections
    ) {
        if (menuSelections == null || menuSelections.isEmpty()) {
            throw new IllegalArgumentException("메뉴를 최소 1개 이상 선택해야 합니다.");
        }

        AtomicInteger sortOrder = new AtomicInteger(0);

        List<ReservationMenuItem> items = menuSelections.stream()
                .map(selection -> {
                    Long menuId = selection.getMenuId();
                    Long tagId = selection.getTagId();
                    if (menuId == null) {
                        throw new IllegalArgumentException("메뉴를 선택해야 합니다.");
                    }
                    if (tagId == null) {
                        throw new IllegalArgumentException("태그를 선택해야 합니다.");
                    }
                    ShopMenu menu = shopMenuReader.getById(menuId);

                    if (!menu.getShopId().equals(shopId)) {
                        throw new IllegalArgumentException(
                                "메뉴(id=" + menuId + ")가 해당 매장(shopId=" + shopId + ")에 속하지 않습니다.");
                    }

                    if (!menu.getIsActive()) {
                        throw new IllegalArgumentException(
                                "비활성 메뉴(id=" + menuId + ")는 선택할 수 없습니다.");
                    }

                    String tagNameSnapshot = resolveTagNameSnapshot(shopId, menuId, tagId);

                    return ReservationMenuItem.create(
                            reservationId,
                            menuId,
                            menu.getName(),
                            tagNameSnapshot,
                            menu.getPrice(),
                            sortOrder.getAndIncrement()
                    );
                })
                .toList();

        return reservationMenuItemWriter.saveAll(items);
    }

    private String resolveTagNameSnapshot(Long shopId, Long menuId, Long tagId) {
        if (tagId == null) {
            throw new IllegalArgumentException("태그를 선택해야 합니다.");
        }

        ShopMenuTag menuTag = shopMenuTagRepository.findByShopMenuIdAndAnyTagId(menuId, tagId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "메뉴(id=" + menuId + ")에 선택한 태그(id=" + tagId + ")가 연결되어 있지 않습니다."));

        if (menuTag.getShopTagId() != null) {
            ShopTag shopTag = shopTagRepository.findByIdAndShopId(menuTag.getShopTagId(), shopId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "선택한 태그(id=" + tagId + ")를 찾을 수 없습니다."));
            return shopTag.getName();
        }

        Tag legacyTag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "선택한 태그(id=" + tagId + ")를 찾을 수 없습니다."));
        return legacyTag.getName();
    }
}
