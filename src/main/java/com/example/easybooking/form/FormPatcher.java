package com.example.easybooking.form;

import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormField;
import com.example.easybooking.form.domain.repository.FormFieldRepository;
import com.example.easybooking.form.dto.FormFieldPatchDto;
import com.example.easybooking.form.dto.FormMetadataPatchDto;
import com.example.easybooking.form.dto.request.FormPatchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FormPatcher {
    private final FormFieldReader formFieldReader;
    private final FormFieldRepository formFieldRepository;
    private final FormFieldOrderManager orderManager;

    public void patch(Form form, FormPatchRequest request) {
        // 메타데이터 처리
        if (request.getMetadata() != null) {
            patchFormMetadata(form, request.getMetadata());
        }
        // 필드 작업 처리
        if (request.getFieldOperations() != null) {
            applyFormFieldPatches(form, request.getFieldOperations());
        }
    }

    private void patchFormMetadata(Form form, FormMetadataPatchDto metadata) {
        if (metadata.getFormName() != null) {
            form.updateName(metadata.getFormName());
        }
        if (metadata.getFormTitle() != null) {
            form.updateTitle(metadata.getFormTitle());
        }
    }

    private void applyFormFieldPatches(Form form, List<FormFieldPatchDto> patches) {

        for (FormFieldPatchDto patch : patches) {
            switch (patch.getOperation()) {
                case ADD:
                    addNewField(form, patch);
                    break;
                case UPDATE:
                    updateExistingField(form, patch);
                    break;
                case DELETE:
                    deleteField(form, patch.getFieldId());
                    break;
            }
        }
    }

    private void addNewField(Form form, FormFieldPatchDto patch) {
        Integer targetOrder = patch.getDisplayOrder();
        shiftFieldsOrder(form, targetOrder, 1);
        FormField newField = FormField.createFrom(patch, form);
        formFieldRepository.save(newField);
    }

    private void updateExistingField(Form form, FormFieldPatchDto patch) {
        FormField existingField = formFieldReader.readByFormAndFieldId(form, patch.getFieldId());
        if(isOrderChange(existingField, patch)) {
            handleOrderChange(form, existingField.getDisplayOrder(), patch.getDisplayOrder());
        }
        existingField.updateFrom(patch);
        formFieldRepository.save(existingField);
    }

    private void deleteField(Form form, String fieldId) {
        FormField fieldToDelete = formFieldReader.readByFormAndFieldId(form, fieldId);
        if (fieldToDelete != null) {
            Integer deletedOrder = fieldToDelete.getDisplayOrder();
            formFieldRepository.delete(fieldToDelete);
            shiftFieldsOrder(form, deletedOrder + 1, -1);
        }
    }

    private boolean isOrderChange(FormField existingField, FormFieldPatchDto patch) {
        Integer newOrder = patch.getDisplayOrder();
        return newOrder != null && !newOrder.equals(existingField.getDisplayOrder());
    }

    private void handleOrderChange(Form form, Integer oldOrder, Integer newOrder) {
        if (newOrder < oldOrder) {
            // 앞으로 이동: 새 위치부터 기존 위치 전까지 뒤로 밀기
            orderManager.shiftFieldsInRange(form, newOrder, oldOrder - 1, 1);
        } else {
            // 뒤로 이동: 기존 위치 다음부터 새 위치까지 앞으로 당기기
            orderManager.shiftFieldsInRange(form, oldOrder + 1, newOrder, -1);
        }
    }



    private void shiftFieldsOrder(Form form, Integer fromOrder, Integer shift) {
        List<FormField> fieldsToShift = formFieldRepository.findFieldsFromOrder(form, fromOrder);

        for (FormField field : fieldsToShift) {
            field.updateDisplayOrder(field.getDisplayOrder() + shift);
        }

        if (!fieldsToShift.isEmpty()) {
            formFieldRepository.saveAll(fieldsToShift);
        }
    }



}
