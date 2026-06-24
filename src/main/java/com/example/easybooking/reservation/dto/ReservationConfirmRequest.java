package com.example.easybooking.reservation.dto;

import com.example.easybooking.common.validation.MultipleOf;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationConfirmRequest {
    // 수락 시 전달사항 필수로 받음
    @NotBlank(message = "수락 시 고객에게 전달할 메시지는 필수 입력 사항입니다.")
    private String message;

    @NotNull(message = "예약 시간은 필수 입력 사항입니다.")
    @Min(value = 30, message = "예약 시간은 30분 이상이어야 합니다.")
    @Max(value = 180, message = "예약 시간은 180분 이하여야 합니다.")
    @MultipleOf(base = 30, message = "예약 시간은 30분 단위여야 합니다.")
    private Integer durationMinutes;

    /**
     * 확정 시 점주가 예약 날짜를 변경할 수 있다.
     * <p>
     * - nullable: 미전달 시 기존 예약 날짜로 확정
     * - 형식: "yyyy-MM-dd"
     */
    private LocalDate date;

    /**
     * 확정 시 점주가 시작 시간을 변경(reschedule)할 수 있다.
     * <p>
     * - nullable: 미전달 시 기존 예약 시간으로 확정
     * - 형식: "HH:mm"
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startAt;

}
