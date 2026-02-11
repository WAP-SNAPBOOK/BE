package com.example.easybooking.shop.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.easybooking.shop.repository.ShopMenuInputFieldRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ShopMenuInputFieldTest {

    @Autowired EntityManager em;
    @Autowired ShopMenuInputFieldRepository repository;

    // --- 3-A-1: 매핑 ---
    @Test
    void canPersistAndLoadInputField() {
        ShopMenuInputField field = ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true,
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE,
                null, null, 0);

        em.persist(field);
        em.flush();
        em.clear();

        ShopMenuInputField found = em.find(ShopMenuInputField.class, field.getId());
        assertThat(found.getId()).isNotNull();
        assertThat(found.getShopMenuId()).isEqualTo(1L);
        assertThat(found.getLabel()).isEqualTo("갯수");
        assertThat(found.getInputType()).isEqualTo(InputType.NUMBER);
        assertThat(found.getRequired()).isTrue();
        assertThat(found.getMinValue()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(found.getMaxValue()).isEqualByComparingTo(BigDecimal.TEN);
    }

    // --- 3-A-2: UNIQUE ---
    @Test
    void save_throwsException_whenDuplicateMenuIdAndLabel() {
        repository.saveAndFlush(ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true,
                null, null, null, null, null, 0));

        assertThatThrownBy(() -> repository.saveAndFlush(ShopMenuInputField.create(
                1L, "갯수", InputType.TEXT, false,
                null, null, null, null, null, 1)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_allowsSameLabelInDifferentMenu() {
        repository.saveAndFlush(ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true, null, null, null, null, null, 0));
        repository.saveAndFlush(ShopMenuInputField.create(
                2L, "갯수", InputType.NUMBER, true, null, null, null, null, null, 0));
        // no exception
    }

    // --- 3-A-3: inputType 제한 ---
    @Test
    void create_acceptsNumberType() {
        ShopMenuInputField field = ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true, null, null, null, null, null, 0);
        assertThat(field.getInputType()).isEqualTo(InputType.NUMBER);
    }

    @Test
    void create_acceptsTextType() {
        ShopMenuInputField field = ShopMenuInputField.create(
                1L, "요청사항", InputType.TEXT, false, null, null, null, 100, null, 0);
        assertThat(field.getInputType()).isEqualTo(InputType.TEXT);
    }

    // --- 3-B-1: NUMBER minValue > maxValue ---
    @Test
    void create_throwsException_whenMinValueGreaterThanMaxValue() {
        assertThatThrownBy(() -> ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true,
                BigDecimal.TEN, BigDecimal.valueOf(5), null, null, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minValue");
    }

    @Test
    void create_allowsMinValueEqualsMaxValue() {
        ShopMenuInputField field = ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true,
                BigDecimal.valueOf(5), BigDecimal.valueOf(5), null, null, null, 0);
        assertThat(field.getMinValue()).isEqualByComparingTo(field.getMaxValue());
    }

    // --- 3-B-2: NUMBER stepValue <= 0 ---
    @Test
    void create_throwsException_whenStepValueZero() {
        assertThatThrownBy(() -> ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true,
                null, null, BigDecimal.ZERO, null, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stepValue");
    }

    @Test
    void create_allowsNullStepValue() {
        ShopMenuInputField field = ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true,
                null, null, null, null, null, 0);
        assertThat(field.getStepValue()).isNull();
    }

    // --- 3-B-3: TEXT maxLength <= 0 ---
    @Test
    void create_throwsException_whenMaxLengthZero() {
        assertThatThrownBy(() -> ShopMenuInputField.create(
                1L, "요청사항", InputType.TEXT, false,
                null, null, null, 0, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxLength");
    }

    @Test
    void create_allowsNullMaxLength() {
        ShopMenuInputField field = ShopMenuInputField.create(
                1L, "요청사항", InputType.TEXT, false,
                null, null, null, null, null, 0);
        assertThat(field.getMaxLength()).isNull();
    }

    // --- 3-B-4: TEXT에 min/max/step 무시 ---
    @Test
    void create_ignoresNumberFieldsForTextType() {
        ShopMenuInputField field = ShopMenuInputField.create(
                1L, "요청사항", InputType.TEXT, false,
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ONE, 100, null, 0);
        assertThat(field.getMinValue()).isNull();
        assertThat(field.getMaxValue()).isNull();
        assertThat(field.getStepValue()).isNull();
        assertThat(field.getMaxLength()).isEqualTo(100);
    }

    // --- 3-B-5: NUMBER에 maxLength 무시 ---
    @Test
    void create_ignoresMaxLengthForNumberType() {
        ShopMenuInputField field = ShopMenuInputField.create(
                1L, "갯수", InputType.NUMBER, true,
                BigDecimal.ONE, BigDecimal.TEN, null, 100, null, 0);
        assertThat(field.getMaxLength()).isNull();
        assertThat(field.getMinValue()).isEqualByComparingTo(BigDecimal.ONE);
    }
}
