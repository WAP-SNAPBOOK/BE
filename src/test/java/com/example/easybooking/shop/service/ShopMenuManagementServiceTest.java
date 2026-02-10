package com.example.easybooking.shop.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.ShopMenuWriter;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.dto.request.CreateShopMenuRequest;
import com.example.easybooking.shop.dto.request.UpdateShopMenuRequest;
import com.example.easybooking.shop.dto.response.ShopMenuResponse;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopMenuManagementServiceTest {

    @Autowired
    ShopMenuRepository shopMenuRepository;

    ShopMenuManagementService service;

    @BeforeEach
    void setUp() {
        shopMenuRepository.deleteAll();
        ShopMenuReader reader = new ShopMenuReader(shopMenuRepository);
        ShopMenuWriter writer = new ShopMenuWriter(shopMenuRepository);
        service = new ShopMenuManagementService(reader, writer);
    }

    @Test
    void create_returnsCreatedMenu() {
        CreateShopMenuRequest request = new CreateShopMenuRequest("젤네일", "기본 젤네일", 0);

        ShopMenuResponse response = service.create(1L, request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getShopId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("젤네일");
        assertThat(response.getDescription()).isEqualTo("기본 젤네일");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void getActiveMenus_returnsOnlyActiveMenusSorted() {
        shopMenuRepository.save(ShopMenu.create(1L, "메뉴B", null, true, 1));
        shopMenuRepository.save(ShopMenu.create(1L, "메뉴A", null, true, 0));
        shopMenuRepository.save(ShopMenu.create(1L, "비활성", null, false, 2));

        List<ShopMenuResponse> result = service.getActiveMenus(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("메뉴A");
        assertThat(result.get(1).getName()).isEqualTo("메뉴B");
    }

    @Test
    void update_changesNameAndDescription() {
        ShopMenu saved = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", "설명", true, 0));

        ShopMenuResponse response = service.update(1L, saved.getId(),
                new UpdateShopMenuRequest("젤아트", "새 설명", 1));

        assertThat(response.getName()).isEqualTo("젤아트");
        assertThat(response.getDescription()).isEqualTo("새 설명");
        assertThat(response.getSortOrder()).isEqualTo(1);
    }

    @Test
    void deactivate_setsIsActiveToFalse() {
        ShopMenu saved = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));

        service.deactivate(1L, saved.getId());

        ShopMenu found = shopMenuRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getIsActive()).isFalse();
    }
}
