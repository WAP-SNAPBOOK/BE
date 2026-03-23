package com.example.easybooking.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopMenuReaderTest {

    @Autowired
    ShopMenuRepository shopMenuRepository;

    ShopMenuReader reader;

    @BeforeEach
    void setUp() {
        shopMenuRepository.deleteAll();
        reader = new ShopMenuReader(shopMenuRepository);
    }

    @Test
    void findActiveByShopId_returnsActiveMenusSortedBySortOrder() {
        shopMenuRepository.save(ShopMenu.create(1L, "메뉴C", null, true, 2));
        shopMenuRepository.save(ShopMenu.create(1L, "메뉴A", null, true, 0));
        shopMenuRepository.save(ShopMenu.create(1L, "메뉴B", null, true, 1));
        shopMenuRepository.save(ShopMenu.create(1L, "비활성메뉴", null, false, 3));

        List<ShopMenu> result = reader.findActiveByShopId(1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("메뉴A");
        assertThat(result.get(1).getName()).isEqualTo("메뉴B");
        assertThat(result.get(2).getName()).isEqualTo("메뉴C");
    }

    @Test
    void findActiveByShopId_returnsEmptyList_whenNoMenus() {
        List<ShopMenu> result = reader.findActiveByShopId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void getById_returnsMenu_whenExists() {
        ShopMenu saved = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));

        ShopMenu found = reader.getById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("젤네일");
    }

    @Test
    void getById_throwsException_whenNotFound() {
        assertThatThrownBy(() -> reader.getById(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getByIdAndShopId_returnsMenu_whenShopMatches() {
        ShopMenu saved = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));

        ShopMenu found = reader.getByIdAndShopId(1L, saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getShopId()).isEqualTo(1L);
    }

    @Test
    void getByIdAndShopId_throwsException_whenShopDoesNotMatch() {
        ShopMenu saved = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));

        assertThatThrownBy(() -> reader.getByIdAndShopId(2L, saved.getId()))
                .isInstanceOf(RuntimeException.class);
    }
}
