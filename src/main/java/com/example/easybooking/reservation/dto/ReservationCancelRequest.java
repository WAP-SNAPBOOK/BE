package com.example.easybooking.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservationCancelRequest {

    @NotBlank(message = "취소 사유는 필수 입력 사항입니다.")
    private String reason;
}
