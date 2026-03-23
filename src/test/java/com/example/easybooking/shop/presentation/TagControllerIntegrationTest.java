package com.example.easybooking.shop.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.shop.repository.ShopTagRepository;
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
