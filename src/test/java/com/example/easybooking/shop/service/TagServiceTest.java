package com.example.easybooking.shop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.shop.ShopMenuReader;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.domain.Tag;
import com.example.easybooking.shop.dto.response.TagResponse;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.ShopTagRepository;
import com.example.easybooking.shop.repository.TagRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TagServiceTest {

    @Autowired TagRepository tagRepository;
    @Autowired ShopTagRepository shopTagRepository;
    @Autowired ShopMenuTagRepository shopMenuTagRepository;
    @Autowired ShopMenuRepository shopMenuRepository;

    TagService service;
    ShopReader shopReader;
    ShopMenuReader shopMenuReader;

    @BeforeEach
    void setUp() {
        shopMenuTagRepository.deleteAll();
        shopMenuRepository.deleteAll();
        shopTagRepository.deleteAll();
        tagRepository.deleteAll();
        shopReader = mock(ShopReader.class);
        shopMenuReader = new ShopMenuReader(shopMenuRepository);
        when(shopReader.isShopOwnedBy(1L, 100L)).thenReturn(true);
        when(shopReader.isShopOwnedBy(2L, 200L)).thenReturn(true);
        service = new TagService(tagRepository, shopReader, shopMenuReader, shopTagRepository, shopMenuTagRepository);
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

        service.addTagToMenu(1L, menu.getId(), tag.getId());

        assertThat(shopMenuTagRepository.findByShopMenuIdAndAnyTagId(menu.getId(), tag.getId()))
                .isPresent();
        assertThat(shopTagRepository.findByShopIdAndName(1L, "손관리")).isPresent();
    }

    @Test
    void createShopTag_createsShopLocalTag_andAllowsSameNameAcrossDifferentShops() {
        TagResponse first = service.createShopTag(1L, "손관리");
        TagResponse second = service.createShopTag(2L, "손관리");

        List<ShopTag> savedTags = shopTagRepository.findAll();

        assertThat(savedTags).hasSize(2);
        assertThat(savedTags)
                .extracting(ShopTag::getShopId, ShopTag::getName, ShopTag::getSortOrder)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(1L, "손관리", 0),
                        org.assertj.core.groups.Tuple.tuple(2L, "손관리", 0)
                );
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void createShopTag_throwsException_whenSameNameAlreadyExistsInSameShop() {
        service.createShopTag(1L, "손관리");

        assertThatThrownBy(() -> service.createShopTag(1L, "손관리"))
                .isInstanceOf(ShopException.class)
                .hasMessageContaining("이미 존재하는 매장 태그");
    }

    @Test
    void getVisibleShopTags_returnsDistinctActiveTagsInStoredOrder() {
        ShopMenu activeFirst = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));
        ShopMenu activeSecond = shopMenuRepository.save(ShopMenu.create(1L, "페디큐어", null, true, 1));
        ShopMenu inactive = shopMenuRepository.save(ShopMenu.create(1L, "왁싱", null, false, 2));
        shopMenuRepository.save(ShopMenu.create(2L, "타매장메뉴", null, true, 0));

        ShopTag secondOrder = shopTagRepository.save(ShopTag.create(1L, "발관리", 1));
        ShopTag firstOrder = shopTagRepository.save(ShopTag.create(1L, "손관리", 0));
        shopTagRepository.save(ShopTag.create(2L, "손관리", 0));

        Tag handGlobalTag = tagRepository.save(Tag.create("손관리"));
        Tag footGlobalTag = tagRepository.save(Tag.create("발관리"));
        Tag inactiveGlobalTag = tagRepository.save(Tag.create("왁싱"));

        shopMenuTagRepository.save(ShopMenuTag.create(activeFirst.getId(), handGlobalTag.getId()));
        shopMenuTagRepository.save(ShopMenuTag.create(activeSecond.getId(), handGlobalTag.getId()));
        shopMenuTagRepository.save(ShopMenuTag.create(activeSecond.getId(), footGlobalTag.getId()));
        shopMenuTagRepository.save(ShopMenuTag.create(inactive.getId(), inactiveGlobalTag.getId()));

        List<TagResponse> result = service.getVisibleShopTags(1L);

        assertThat(result)
                .extracting(TagResponse::getId, TagResponse::getName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(firstOrder.getId(), "손관리"),
                        org.assertj.core.groups.Tuple.tuple(secondOrder.getId(), "발관리")
                );
    }

    @Test
    void removeTagFromMenu_deletesLink() {
        ShopMenu menu = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));
        Tag tag = tagRepository.save(Tag.create("손관리"));
        shopMenuTagRepository.save(ShopMenuTag.create(menu.getId(), tag.getId()));

        service.removeTagFromMenu(1L, menu.getId(), tag.getId());

        assertThat(shopMenuTagRepository.findByShopMenuIdAndAnyTagId(menu.getId(), tag.getId()))
                .isEmpty();
    }

    @Test
    void addTagToMenu_supportsShopTagIdAndBackfillsLegacyTagId() {
        ShopMenu menu = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));
        ShopTag shopTag = shopTagRepository.save(ShopTag.create(1L, "손관리", 0));

        service.addTagToMenu(1L, menu.getId(), shopTag.getId());

        ShopMenuTag saved = shopMenuTagRepository.findByShopMenuIdAndShopTagId(menu.getId(), shopTag.getId())
                .orElseThrow();
        assertThat(saved.getShopTagId()).isEqualTo(shopTag.getId());
        assertThat(saved.getTagId()).isNotNull();
        assertThat(tagRepository.findById(saved.getTagId())).isPresent();
    }

    @Test
    void updateShopTagOrder_movesVisibleTagsFirstAndKeepsHiddenRelativeOrder() {
        ShopTag visibleFirst = shopTagRepository.save(ShopTag.create(1L, "손관리", 0));
        ShopTag hiddenFirst = shopTagRepository.save(ShopTag.create(1L, "숨김1", 1));
        ShopTag visibleSecond = shopTagRepository.save(ShopTag.create(1L, "발관리", 2));
        ShopTag hiddenSecond = shopTagRepository.save(ShopTag.create(1L, "숨김2", 3));

        ShopMenu activeMenu = shopMenuRepository.save(ShopMenu.create(1L, "젤네일", null, true, 0));
        Tag handGlobalTag = tagRepository.save(Tag.create("손관리"));
        Tag footGlobalTag = tagRepository.save(Tag.create("발관리"));
        shopMenuTagRepository.save(ShopMenuTag.createResolved(activeMenu.getId(), handGlobalTag.getId(), visibleFirst.getId()));
        shopMenuTagRepository.save(ShopMenuTag.createResolved(activeMenu.getId(), footGlobalTag.getId(), visibleSecond.getId()));

        service.updateShopTagOrder(1L, 100L, List.of(visibleSecond.getId(), visibleFirst.getId()));

        assertThat(shopTagRepository.findByShopIdOrderBySortOrderAsc(1L))
                .extracting(ShopTag::getId, ShopTag::getName, ShopTag::getSortOrder)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(visibleSecond.getId(), "발관리", 0),
                        org.assertj.core.groups.Tuple.tuple(visibleFirst.getId(), "손관리", 1),
                        org.assertj.core.groups.Tuple.tuple(hiddenFirst.getId(), "숨김1", 2),
                        org.assertj.core.groups.Tuple.tuple(hiddenSecond.getId(), "숨김2", 3)
                );
    }
}
