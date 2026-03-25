package com.example.easybooking.auth.dev;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.auth.dev.presentation.DevAuthController;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

class DevAuthProfileAnnotationTest {

    @Test
    void devAuthController_isRestrictedToLocalAndTestProfiles() {
        Profile profile = DevAuthController.class.getAnnotation(Profile.class);

        assertThat(profile).isNotNull();
        assertThat(Arrays.asList(profile.value()))
                .containsExactlyInAnyOrder("local", "test");
    }
}
