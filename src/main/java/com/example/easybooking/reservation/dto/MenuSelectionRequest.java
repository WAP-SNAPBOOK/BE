package com.example.easybooking.reservation.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuSelectionRequest {
    private Long menuId;
    private List<MenuInputValueRequest> inputValues;
}
