package com.example.easybooking.form.dto;

import com.example.easybooking.form.domain.type.FieldType;
import lombok.Builder;

import java.util.List;

@Builder
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
