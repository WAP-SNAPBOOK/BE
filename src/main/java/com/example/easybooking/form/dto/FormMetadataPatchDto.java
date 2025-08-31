package com.example.easybooking.form.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FormMetadataPatchDto {
    private String formName;
    private String formTitle;
}