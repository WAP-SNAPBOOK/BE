package com.example.easybooking.slot;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Data
public class AddSlotDto {
    List<LocalDateTime> startDateTimes;
}
