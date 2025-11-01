package com.example.easybooking.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservationRejectRequest {
    // 거절 사유 필수로 받음
    @NotBlank(message = "거절 사유는 필수 입력 사항입니다.")
    private String reason;
}
