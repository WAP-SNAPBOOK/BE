package com.example.easybooking.form.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class FormResponse {
    private String id;
    private String name;
    private String title;
    private List<FormFieldDto> formFields;
}
