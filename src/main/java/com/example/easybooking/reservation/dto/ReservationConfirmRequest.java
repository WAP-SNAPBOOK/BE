package com.example.easybooking.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservationConfirmRequest {
    // 수락 시 전달사항 필수로 받음
    @NotBlank(message = "수락 시 고객에게 전달할 메시지는 필수 입력 사항입니다.")
    private String message;

}
