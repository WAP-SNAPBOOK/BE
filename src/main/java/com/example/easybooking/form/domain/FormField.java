package com.example.easybooking.form.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.easybooking.form.domain.type.FieldType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class FormField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_config_id")
    private Form form;

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

    @ElementCollection
    @CollectionTable(
            name = "form_field_options",
            joinColumns = @JoinColumn(name = "form_field_id")
    )
    @OrderColumn(name = "option_order")
    @Column(name = "option_value")
    private List<String> options = new ArrayList<>();


    public static FormField copyFrom(FormField original, Form newForm) {
        FormField copy = new FormField();
        copy.fieldId = original.getFieldId();
        copy.label = original.getLabel();
        copy.type = original.getType();
        copy.required = original.getRequired();
        copy.placeholder = original.getPlaceholder();
        copy.minValue = original.getMinValue();
        copy.maxValue = original.getMaxValue();
        copy.acceptTypes = original.getAcceptTypes();
        copy.displayOrder = original.getDisplayOrder();
        copy.form = newForm;

        if (original.getOptions() != null && !original.getOptions().isEmpty()) {
            copy.setOptions(new ArrayList<>(original.getOptions()));
        }

        return copy;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }

    public List<String> getOptions() {
        return this.options != null ? this.options : new ArrayList<>();
    }
}
