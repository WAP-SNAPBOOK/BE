package com.example.easybooking.shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.example.easybooking.form.FormService;
import com.example.easybooking.shop.dto.request.CreateShopRequest;
import com.example.easybooking.shop.dto.response.CreateShopResponse;
import com.example.easybooking.shop.service.ShopService;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import com.example.easybooking.user.domain.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ShopDefaultStaffCreationTest {

    @Autowired
    ShopService shopService;

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    UserRepository userRepository;

    @MockitoBean
    FormService formService;

    @Test
    void createShop_createsDefaultStaff() {
        User owner = userRepository.save(User.createUser(
                "provider-shop-default-staff",
                "owner",
                "010-0000-0000",
                UserType.OWNER
        ));

        CreateShopRequest request = CreateShopRequest.builder()
                .businessName("테스트샵")
                .address("서울")
                .businessNumber("123-45-67890")
                .build();

        CreateShopResponse response = shopService.createShop(owner.getId(), request);

        List<Staff> staffs = staffRepository.findByShopId(response.getShopId());
        assertThat(staffs).hasSize(1);
        assertThat(staffs.get(0).getName()).isEqualTo("기본");

        verify(formService).createDefaultForm(response.getShopId());
    }
}

