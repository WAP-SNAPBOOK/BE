package com.example.easybooking.reservation.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation_menu_input_values")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationMenuInputValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_menu_item_id", nullable = false)
    private Long reservationMenuItemId;

    @Column(name = "shop_menu_input_field_id", nullable = false)
    private Long shopMenuInputFieldId;

    @Column(name = "field_label_snapshot", nullable = false)
    private String fieldLabelSnapshot;

    @Column(name = "input_type_snapshot", nullable = false, length = 20)
    private String inputTypeSnapshot;

    @Column(name = "value_number", precision = 10, scale = 2)
    private BigDecimal valueNumber;

    @Column(name = "value_text")
    private String valueText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ReservationMenuInputValue create(Long reservationMenuItemId,
                                                   Long shopMenuInputFieldId,
                                                   String fieldLabelSnapshot,
                                                   String inputTypeSnapshot,
                                                   BigDecimal valueNumber,
                                                   String valueText) {
        ReservationMenuInputValue value = new ReservationMenuInputValue();
        value.reservationMenuItemId = reservationMenuItemId;
        value.shopMenuInputFieldId = shopMenuInputFieldId;
        value.fieldLabelSnapshot = fieldLabelSnapshot;
        value.inputTypeSnapshot = inputTypeSnapshot;
        value.valueNumber = valueNumber;
        value.valueText = valueText;
        return value;
    }
}
