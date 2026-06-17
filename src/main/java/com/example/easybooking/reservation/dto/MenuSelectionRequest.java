package com.example.easybooking.reservation.dto;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuSelectionRequest {
    @NotNull
    private Long menuId;
    @NotNull
    private Long tagId;
    private List<MenuInputValueRequest> inputValues;
}
