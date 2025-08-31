package com.example.easybooking.form.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.easybooking.form.domain.type.FieldType;

import com.example.easybooking.form.dto.FormFieldPatchDto;
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

    public static FormField createFrom(FormFieldPatchDto patch,Form form){
        FormField field = new FormField();
        field.form = form;
        field.fieldId = patch.getFieldId();
        field.label = patch.getLabel();
        field.type = patch.getType();
        field.required = patch.getRequired() != null ? patch.getRequired() : false;
        field.placeholder = patch.getPlaceholder();
        field.minValue = patch.getMinValue();
        field.maxValue = patch.getMaxValue();
        field.acceptTypes = patch.getAcceptTypes();
        field.displayOrder = patch.getDisplayOrder() != null ? patch.getDisplayOrder() : 0;
        if (patch.getOptions() != null) {
            field.setOptions(patch.getOptions());
        }
        return field;
    }

    public void updateFrom(FormFieldPatchDto patchDto) {
        if (patchDto.getLabel() != null) {
            this.label = patchDto.getLabel();
        }
        if (patchDto.getType() != null) {
            this.type = patchDto.getType();
        }
        if (patchDto.getRequired() != null) {
            this.required = patchDto.getRequired();
        }
        if (patchDto.getPlaceholder() != null) {
            this.placeholder = patchDto.getPlaceholder();
        }
        if (patchDto.getMinValue() != null) {
            this.minValue = patchDto.getMinValue();
        }
        if (patchDto.getMaxValue() != null) {
            this.maxValue = patchDto.getMaxValue();
        }
        if (patchDto.getAcceptTypes() != null) {
            this.acceptTypes = patchDto.getAcceptTypes();
        }
        if (patchDto.getDisplayOrder() != null) {
            this.displayOrder = patchDto.getDisplayOrder();
        }
        if (patchDto.getOptions() != null) {
            this.setOptions(patchDto.getOptions());
        }
    }

    public void updateDisplayOrder(Integer newOrder) {
        this.displayOrder = newOrder;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }

    public List<String> getOptions() {
        return this.options != null ? this.options : new ArrayList<>();
    }
}
