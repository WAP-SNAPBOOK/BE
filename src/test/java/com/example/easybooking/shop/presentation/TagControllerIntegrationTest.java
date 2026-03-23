package com.example.easybooking.shop.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.domain.Tag;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.shop.repository.ShopTagRepository;
import com.example.easybooking.shop.repository.TagRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TagControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    ShopMenuRepository shopMenuRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ShopMenuTagRepository shopMenuTagRepository;

    @Autowired
    ShopTagRepository shopTagRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShopTag_createsTagForOwnedShop() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-tag-owner", "tag-owner", "01099991111", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("태그샵")
                        .address("서울")
                        .businessNumber("100-20-30000")
                        .build()
        ));
        authenticate(owner.getId());

        mockMvc.perform(post("/api/shops/{shopId}/tags", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "손관리"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("손관리"));

        org.assertj.core.api.Assertions.assertThat(shopTagRepository.findByShopIdOrderBySortOrderAsc(shop.getId()))
                .extracting(ShopTag::getName, ShopTag::getSortOrder)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("손관리", 0));
    }

    @Test
    void createShopTag_returnsForbidden_whenUserIsNotShopOwner() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-tag-owner-2", "tag-owner-2", "01099992222", UserType.OWNER)
        );
        User otherOwner = userRepository.saveAndFlush(
                User.createUser("kakao-tag-owner-3", "tag-owner-3", "01099993333", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("권한샵")
                        .address("서울")
                        .businessNumber("100-20-30001")
                        .build()
        ));
        authenticate(otherOwner.getId());

        mockMvc.perform(post("/api/shops/{shopId}/tags", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "손관리"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SHOP_OWNER_MISMATCH"));
    }

    @Test
    void createShopTag_returnsConflict_whenTagNameAlreadyExistsInSameShop() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-tag-owner-dup", "tag-owner-dup", "01099997777", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("중복태그샵")
                        .address("서울")
                        .businessNumber("100-20-30005")
                        .build()
        ));
        shopTagRepository.saveAndFlush(ShopTag.create(shop.getId(), "손관리", 0));
        authenticate(owner.getId());

        mockMvc.perform(post("/api/shops/{shopId}/tags", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "손관리"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SHOP_TAG_ALREADY_EXISTS"));
    }

    @Test
    void getVisibleShopTags_returnsOnlyActiveDistinctTagsInStoredOrder() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-tag-owner-4", "tag-owner-4", "01099994444", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("조회샵")
                        .address("서울")
                        .businessNumber("100-20-30002")
                        .build()
        ));

        ShopMenu activeFirst = shopMenuRepository.saveAndFlush(ShopMenu.create(shop.getId(), "젤네일", null, true, 0));
        ShopMenu activeSecond = shopMenuRepository.saveAndFlush(ShopMenu.create(shop.getId(), "페디큐어", null, true, 1));
        ShopMenu inactive = shopMenuRepository.saveAndFlush(ShopMenu.create(shop.getId(), "비활성", null, false, 2));

        ShopTag secondOrder = shopTagRepository.saveAndFlush(ShopTag.create(shop.getId(), "발관리-조회", 1));
        ShopTag firstOrder = shopTagRepository.saveAndFlush(ShopTag.create(shop.getId(), "손관리-조회", 0));

        Tag handGlobalTag = tagRepository.saveAndFlush(Tag.create("손관리-조회"));
        Tag footGlobalTag = tagRepository.saveAndFlush(Tag.create("발관리-조회"));
        Tag inactiveGlobalTag = tagRepository.saveAndFlush(Tag.create("비활성전용-조회"));

        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(activeFirst.getId(), handGlobalTag.getId()));
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(activeSecond.getId(), handGlobalTag.getId()));
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(activeSecond.getId(), footGlobalTag.getId()));
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.create(inactive.getId(), inactiveGlobalTag.getId()));

        mockMvc.perform(get("/api/shops/{shopId}/tags", shop.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstOrder.getId()))
                .andExpect(jsonPath("$[0].name").value("손관리-조회"))
                .andExpect(jsonPath("$[1].id").value(secondOrder.getId()))
                .andExpect(jsonPath("$[1].name").value("발관리-조회"));
    }

    @Test
    void updateShopTagOrder_reordersVisibleTagsAndKeepsHiddenAfterThem() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("kakao-tag-owner-5", "tag-owner-5", "01099995555", UserType.OWNER)
        );
        Shop shop = shopRepository.saveAndFlush(Shop.create(
                owner.getId(),
                CreateShopRequest.builder()
                        .businessName("정렬샵")
                        .address("서울")
                        .businessNumber("100-20-30003")
                        .build()
        ));
        ShopTag visibleFirst = shopTagRepository.saveAndFlush(ShopTag.create(shop.getId(), "손관리-정렬", 0));
        ShopTag hidden = shopTagRepository.saveAndFlush(ShopTag.create(shop.getId(), "숨김-정렬", 1));
        ShopTag visibleSecond = shopTagRepository.saveAndFlush(ShopTag.create(shop.getId(), "발관리-정렬", 2));

        ShopMenu activeMenu = shopMenuRepository.saveAndFlush(ShopMenu.create(shop.getId(), "젤네일", null, true, 0));
        Tag handGlobalTag = tagRepository.saveAndFlush(Tag.create("손관리-정렬"));
        Tag footGlobalTag = tagRepository.saveAndFlush(Tag.create("발관리-정렬"));
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.createResolved(activeMenu.getId(), handGlobalTag.getId(), visibleFirst.getId()));
        shopMenuTagRepository.saveAndFlush(ShopMenuTag.createResolved(activeMenu.getId(), footGlobalTag.getId(), visibleSecond.getId()));
        authenticate(owner.getId());

        mockMvc.perform(put("/api/shops/{shopId}/tags/order", shop.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tagIds": [%d, %d]
                                }
                                """.formatted(visibleSecond.getId(), visibleFirst.getId())))
                .andExpect(status().isOk());

        org.assertj.core.api.Assertions.assertThat(shopTagRepository.findByShopIdOrderBySortOrderAsc(shop.getId()))
                .extracting(ShopTag::getId, ShopTag::getName, ShopTag::getSortOrder)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(visibleSecond.getId(), "발관리-정렬", 0),
                        org.assertj.core.groups.Tuple.tuple(visibleFirst.getId(), "손관리-정렬", 1),
                        org.assertj.core.groups.Tuple.tuple(hidden.getId(), "숨김-정렬", 2)
                );
    }

    private void authenticate(Long userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(userId, "OWNER"),
                null,
                null
        ));
        SecurityContextHolder.setContext(context);
    }
}
