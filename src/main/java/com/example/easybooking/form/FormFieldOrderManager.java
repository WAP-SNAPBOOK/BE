package com.example.easybooking.form;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;
import com.example.easybooking.form.domain.repository.FormFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FormFieldOrderManager {
    private final FormFieldRepository formFieldRepository;

    /**
     * 지정된 순서부터 모든 필드를 shift만큼 이동
     */
    public void shiftFieldsFrom(Form form, Integer fromOrder, Integer shift) {
        if (fromOrder == null) return;

        List<FormField> fieldsToShift = formFieldRepository.findFieldsFromOrder(form, fromOrder);

        updateFieldsOrder(fieldsToShift, shift);
    }

    /**
     * 지정된 범위의 필드들을 shift만큼 이동
     */
    public void shiftFieldsInRange(Form form, Integer fromOrder, Integer toOrder, Integer shift) {
        if (fromOrder == null || toOrder == null) return;

        List<FormField> fieldsToShift = formFieldRepository.findFieldsInRange(form, fromOrder, toOrder);

        updateFieldsOrder(fieldsToShift, shift);
    }

    private void updateFieldsOrder(List<FormField> fields, Integer shift) {
        if (fields.isEmpty()) return;

        for (FormField field : fields) {
            field.updateDisplayOrder(field.getDisplayOrder() + shift);
        }

        formFieldRepository.saveAll(fields);
    }
}