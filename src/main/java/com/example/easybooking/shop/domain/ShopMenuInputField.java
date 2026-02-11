package com.example.easybooking.shop.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "shop_menu_input_fields",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_shop_menu_input_fields",
                columnNames = {"shop_menu_id", "label"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopMenuInputField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_menu_id", nullable = false)
    private Long shopMenuId;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 20)
    private InputType inputType;

    @Column(nullable = false)
    private Boolean required;

    @Column(name = "min_value", precision = 10, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 10, scale = 2)
    private BigDecimal maxValue;

    @Column(name = "step_value", precision = 10, scale = 2)
    private BigDecimal stepValue;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(length = 255)
    private String placeholder;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ShopMenuInputField create(Long shopMenuId, String label, InputType inputType,
                                            Boolean required, BigDecimal minValue, BigDecimal maxValue,
                                            BigDecimal stepValue, Integer maxLength,
                                            String placeholder, Integer sortOrder) {
        ShopMenuInputField field = new ShopMenuInputField();
        field.shopMenuId = shopMenuId;
        field.label = label;
        field.inputType = inputType;
        field.required = required;
        field.sortOrder = sortOrder;
        field.placeholder = placeholder;
        field.isActive = true;

        // 타입별 필드 정합성: 관련 없는 필드는 무시
        if (inputType == InputType.NUMBER) {
            field.minValue = minValue;
            field.maxValue = maxValue;
            field.stepValue = stepValue;
            field.maxLength = null;  // NUMBER에 maxLength 무시
        } else {
            field.minValue = null;   // TEXT에 min/max/step 무시
            field.maxValue = null;
            field.stepValue = null;
            field.maxLength = maxLength;
        }

        field.validate();
        return field;
    }

    public void update(String label, BigDecimal minValue, BigDecimal maxValue,
                       BigDecimal stepValue, Integer maxLength, String placeholder, Integer sortOrder) {
        if (label != null) this.label = label;
        if (placeholder != null) this.placeholder = placeholder;
        if (sortOrder != null) this.sortOrder = sortOrder;

        if (this.inputType == InputType.NUMBER) {
            if (minValue != null) this.minValue = minValue;
            if (maxValue != null) this.maxValue = maxValue;
            if (stepValue != null) this.stepValue = stepValue;
        } else {
            if (maxLength != null) this.maxLength = maxLength;
        }

        validate();
    }

    public void deactivate() {
        this.isActive = false;
    }

    private void validate() {
        if (inputType == InputType.NUMBER) {
            if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
                throw new IllegalArgumentException("minValue must be <= maxValue");
            }
            if (stepValue != null && stepValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("stepValue must be > 0");
            }
        }
        if (inputType == InputType.TEXT) {
            if (maxLength != null && maxLength <= 0) {
                throw new IllegalArgumentException("maxLength must be > 0");
            }
        }
    }
}
