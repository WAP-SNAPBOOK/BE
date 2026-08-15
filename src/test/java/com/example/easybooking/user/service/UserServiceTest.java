package com.example.easybooking.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.UserWriter;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.dto.UpdateUserProfileRequest;
import com.example.easybooking.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserWriter userWriter;

    @Mock
    private UserReader userReader;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUserProfileChangesNameAndPhoneNumber() {
        User user = User.createUser("provider-id", "이전 이름", "01011112222", UserType.CUSTOMER);
        when(userReader.read(1L)).thenReturn(user);

        UserResponse response = userService.updateUserProfile(
                1L,
                new UpdateUserProfileRequest("새 이름", "01033334444")
        );

        assertThat(response.getName()).isEqualTo("새 이름");
        assertThat(response.getPhoneNumber()).isEqualTo("01033334444");
        assertThat(user.getName()).isEqualTo("새 이름");
        assertThat(user.getPhoneNumber()).isEqualTo("01033334444");
    }

    @Test
    void updateUserProfileTrimsName() {
        User user = User.createUser("provider-id", "이전 이름", "01011112222", UserType.OWNER);
        when(userReader.read(2L)).thenReturn(user);

        UserResponse response = userService.updateUserProfile(
                2L,
                new UpdateUserProfileRequest("  새 이름  ", "01055556666")
        );

        assertThat(response.getName()).isEqualTo("새 이름");
    }
}
