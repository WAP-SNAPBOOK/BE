package com.example.easybooking.form.dto;

import java.util.List;

import com.example.easybooking.form.domain.type.FieldType;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FormFieldDto {
    private String fieldId;
    private String label;
    private FieldType type;
    private Boolean required;
    private String placeholder;
    private Integer min;
    private Integer max;
    private String accept;
    private List<String> options;
}
