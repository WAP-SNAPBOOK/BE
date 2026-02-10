package com.example.easybooking.shop.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.domain.Tag;
import com.example.easybooking.shop.dto.response.TagResponse;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TagServiceTest {

    @Autowired TagRepository tagRepository;
    @Autowired ShopMenuTagRepository shopMenuTagRepository;
    @Autowired ShopMenuRepository shopMenuRepository;

    TagService service;

    @BeforeEach
    void setUp() {
        shopMenuTagRepository.deleteAll();
        shopMenuRepository.deleteAll();
        tagRepository.deleteAll();
        service = new TagService(tagRepository, shopMenuTagRepository);
    }

    @Test
    void createOrGet_createsNewTag() {
        TagResponse response = service.createOrGet("손관리");

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("손관리");
    }

    @Test
    void createOrGet_returnsExistingTag_whenDuplicate() {
        TagResponse first = service.createOrGet("손관리");
        TagResponse second = service.createOrGet("손관리");

        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    void getAllTags_returnsAllTags() {
        tagRepository.save(Tag.create("손관리"));
        tagRepository.save(Tag.create("발관리"));

        List<TagResponse> result = service.getAllTags();

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllTags_returnsEmptyList_whenNoTags() {
        List<TagResponse> result = service.getAllTags();

        assertThat(result).isEmpty();
    }

    @Test
    void addTagToMenu_createsLink() {
        ShopMenu menu = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));
        Tag tag = tagRepository.save(Tag.create("손관리"));

        service.addTagToMenu(menu.getId(), tag.getId());

        assertThat(shopMenuTagRepository.findByShopMenuIdAndTagId(menu.getId(), tag.getId()))
                .isPresent();
    }

    @Test
    void removeTagFromMenu_deletesLink() {
        ShopMenu menu = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));
        Tag tag = tagRepository.save(Tag.create("손관리"));
        shopMenuTagRepository.save(ShopMenuTag.create(menu.getId(), tag.getId()));

        service.removeTagFromMenu(menu.getId(), tag.getId());

        assertThat(shopMenuTagRepository.findByShopMenuIdAndTagId(menu.getId(), tag.getId()))
                .isEmpty();
    }
}
