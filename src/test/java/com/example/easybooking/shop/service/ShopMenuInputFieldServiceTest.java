package com.example.easybooking.shop.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.easybooking.shop.ShopMenuInputFieldReader;
import com.example.easybooking.shop.ShopMenuInputFieldWriter;
import com.example.easybooking.shop.domain.InputType;
import com.example.easybooking.shop.domain.ShopMenuInputField;
import com.example.easybooking.shop.dto.request.CreateInputFieldRequest;
import com.example.easybooking.shop.dto.request.UpdateInputFieldRequest;
import com.example.easybooking.shop.dto.response.InputFieldResponse;
import com.example.easybooking.shop.repository.ShopMenuInputFieldRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShopMenuInputFieldServiceTest {

    @Autowired ShopMenuInputFieldRepository repository;

    ShopMenuInputFieldService service;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        ShopMenuInputFieldReader reader = new ShopMenuInputFieldReader(repository);
        ShopMenuInputFieldWriter writer = new ShopMenuInputFieldWriter(repository);
        service = new ShopMenuInputFieldService(reader, writer);
    }

    @Test
    void create_returnsCreatedField() {
        CreateInputFieldRequest request = new CreateInputFieldRequest(
                "손연장 갯수", "NUMBER", true,
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE,
                null, null, 0);

        InputFieldResponse response = service.create(1L, request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getLabel()).isEqualTo("손연장 갯수");
        assertThat(response.getInputType()).isEqualTo("NUMBER");
        assertThat(response.getRequired()).isTrue();
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void getActiveFields_returnsOnlyActiveSorted() {
        repository.save(ShopMenuInputField.create(
                1L, "필드B", InputType.TEXT, false, null, null, null, null, null, 1));
        repository.save(ShopMenuInputField.create(
                1L, "필드A", InputType.NUMBER, true, null, null, null, null, null, 0));
        ShopMenuInputField inactive = ShopMenuInputField.create(
                1L, "비활성", InputType.TEXT, false, null, null, null, null, null, 2);
        repository.save(inactive);
        inactive.deactivate();
        repository.save(inactive);

        List<InputFieldResponse> result = service.getActiveFields(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLabel()).isEqualTo("필드A");
        assertThat(result.get(1).getLabel()).isEqualTo("필드B");
    }

    @Test
    void update_changesLabel() {
        ShopMenuInputField saved = repository.save(ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true,
                BigDecimal.ONE, BigDecimal.TEN, null, null, null, 0));

        InputFieldResponse response = service.update(saved.getId(),
                new UpdateInputFieldRequest("수량", null, null, null, null, null, null));

        assertThat(response.getLabel()).isEqualTo("수량");
    }

    @Test
    void deactivate_setsIsActiveToFalse() {
        ShopMenuInputField saved = repository.save(ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true, null, null, null, null, null, 0));

        service.deactivate(saved.getId());

        ShopMenuInputField found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getIsActive()).isFalse();
    }
}
