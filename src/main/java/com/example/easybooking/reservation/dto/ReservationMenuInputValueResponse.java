package com.example.easybooking.reservation.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationMenuInputValueResponse {
    private final String fieldLabelSnapshot;
    private final String inputTypeSnapshot;
    private final BigDecimal valueNumber;
    private final String valueText;
}
