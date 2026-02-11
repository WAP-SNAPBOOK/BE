package com.example.easybooking.shop.service;

import com.example.easybooking.shop.ShopMenuInputFieldReader;
import com.example.easybooking.shop.ShopMenuInputFieldWriter;
import com.example.easybooking.shop.domain.InputType;
import com.example.easybooking.shop.domain.ShopMenuInputField;
import com.example.easybooking.shop.dto.request.CreateInputFieldRequest;
import com.example.easybooking.shop.dto.request.UpdateInputFieldRequest;
import com.example.easybooking.shop.dto.response.InputFieldResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopMenuInputFieldService {

    private final ShopMenuInputFieldReader reader;
    private final ShopMenuInputFieldWriter writer;

    @Transactional
    public InputFieldResponse create(Long menuId, CreateInputFieldRequest request) {
        InputType type = InputType.valueOf(request.getInputType());
        ShopMenuInputField field = ShopMenuInputField.create(
                menuId, request.getLabel(), type, request.getRequired(),
                request.getMinValue(), request.getMaxValue(), request.getStepValue(),
                request.getMaxLength(), request.getPlaceholder(), request.getSortOrder());
        return new InputFieldResponse(writer.save(field));
    }

    public List<InputFieldResponse> getActiveFields(Long menuId) {
        return reader.findActiveByMenuId(menuId).stream()
                .map(InputFieldResponse::new)
                .toList();
    }

    @Transactional
    public InputFieldResponse update(Long fieldId, UpdateInputFieldRequest request) {
        ShopMenuInputField field = reader.getById(fieldId);
        field.update(request.getLabel(), request.getMinValue(), request.getMaxValue(),
                request.getStepValue(), request.getMaxLength(), request.getPlaceholder(),
                request.getSortOrder());
        return new InputFieldResponse(field);
    }

    @Transactional
    public void deactivate(Long fieldId) {
        ShopMenuInputField field = reader.getById(fieldId);
        field.deactivate();
    }
}
