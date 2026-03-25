package com.example.easybooking.auth.dev.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
class DevAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void login_returnsLoginSuccess_whenPersonaAlreadySignedUp() throws Exception {
        User owner = userRepository.saveAndFlush(
                User.createUser("dev-owner-1001", "dev-owner", "01012341234", UserType.OWNER)
        );

        mockMvc.perform(post("/dev/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "personaKey": "owner-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authStatus").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$.userId").value(owner.getId()))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.userType").value("OWNER"));
    }

    @Test
    void login_returnsSignupRequired_whenPersonaHasNoUser() throws Exception {
        mockMvc.perform(post("/dev/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "personaKey": "new-customer-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authStatus").value("SIGNUP_REQUIRED"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void getPersonas_returnsSignedUpFlagsForKnownPersonas() throws Exception {
        userRepository.saveAndFlush(
                User.createUser("dev-owner-1001", "dev-owner", "01011112222", UserType.OWNER)
        );

        mockMvc.perform(get("/dev/auth/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].personaKey").value("owner-1"))
                .andExpect(jsonPath("$[0].signedUp").value(true))
                .andExpect(jsonPath("$[3].personaKey").value("new-owner-1"))
                .andExpect(jsonPath("$[3].signedUp").value(false));
    }
}
