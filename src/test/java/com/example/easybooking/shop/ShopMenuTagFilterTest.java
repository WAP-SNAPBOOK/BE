package com.example.easybooking.shop;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.domain.Tag;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopMenuTagFilterTest {

    @Autowired ShopMenuRepository shopMenuRepository;
    @Autowired TagRepository tagRepository;
    @Autowired ShopMenuTagRepository shopMenuTagRepository;

    ShopMenuReader reader;

    @BeforeEach
    void setUp() {
        shopMenuTagRepository.deleteAll();
        shopMenuRepository.deleteAll();
        tagRepository.deleteAll();
        reader = new ShopMenuReader(shopMenuRepository);
    }

    @Test
    void findActiveByShopIdAndTagIds_returnsMenusWithTag() {
        ShopMenu m1 = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));
        ShopMenu m2 = shopMenuRepository.save(ShopMenu.create(1L, "페디큐어", null, true, 1));
        shopMenuRepository.save(ShopMenu.create(1L, "왁싱", null, true, 2));

        Tag t1 = tagRepository.save(Tag.create("손관리"));
        shopMenuTagRepository.save(ShopMenuTag.create(m1.getId(), t1.getId()));
        shopMenuTagRepository.save(ShopMenuTag.create(m2.getId(), t1.getId()));

        List<ShopMenu> result = reader.findActiveByShopIdAndTagIds(1L, List.of(t1.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ShopMenu::getName).containsExactly("젤네일", "페디큐어");
    }

    @Test
    void findActiveByShopIdAndTagIds_orFilter_multipleTagIds() {
        ShopMenu m1 = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));
        ShopMenu m2 = shopMenuRepository.save(ShopMenu.create(1L, "페디큐어", null, true, 1));

        Tag t1 = tagRepository.save(Tag.create("손관리"));
        Tag t2 = tagRepository.save(Tag.create("발관리"));
        shopMenuTagRepository.save(ShopMenuTag.create(m1.getId(), t1.getId()));
        shopMenuTagRepository.save(ShopMenuTag.create(m2.getId(), t2.getId()));

        List<ShopMenu> result = reader.findActiveByShopIdAndTagIds(1L, List.of(t1.getId(), t2.getId()));

        assertThat(result).hasSize(2);
    }

    @Test
    void findActiveByShopIdAndTagIds_excludesInactiveMenus() {
        ShopMenu m1 = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, false, 0));
        Tag t1 = tagRepository.save(Tag.create("손관리"));
        shopMenuTagRepository.save(ShopMenuTag.create(m1.getId(), t1.getId()));

        List<ShopMenu> result = reader.findActiveByShopIdAndTagIds(1L, List.of(t1.getId()));

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveByShopIdAndTagIds_returnsEmpty_whenTagNotFound() {
        List<ShopMenu> result = reader.findActiveByShopIdAndTagIds(1L, List.of(999L));

        assertThat(result).isEmpty();
    }
}
