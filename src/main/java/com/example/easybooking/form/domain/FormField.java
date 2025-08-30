package com.example.easybooking.form.domain;

import com.example.easybooking.form.domain.type.FieldType;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class FormField {
    @Id
    private Long id;

    @Column(nullable = false)
    private String fieldId; // "name", "removal", "wrapping" 등

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType type;

    @Column(nullable = false)
    private Boolean required = false;

    private String placeholder;

    // number 타입 필드를 위한 속성들
    private Integer minValue;
    private Integer maxValue;

    // file 타입 필드를 위한 속성
    private String acceptTypes;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @ManyToOne
    @JoinColumn(name = "form_config_id")
    private Form form;

}
