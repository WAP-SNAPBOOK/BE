package com.example.easybooking.form.domain;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.easybooking.errors.errorcode.ShopErrorCode;
import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.repository.ShopRepository;
import org.springframework.stereotype.Component;

import com.example.easybooking.form.FormFieldReader;
import com.example.easybooking.form.FormReader;
import com.example.easybooking.form.domain.repository.FormFieldRepository;
import com.example.easybooking.form.domain.repository.FormRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FormCopyUtil {

    private final FormReader formReader;
    private final FormFieldReader formFieldReader;
    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;
    private final ShopRepository shopRepository;

    public void copyDefaultFormToShop(Long shopId) {
        // 기본 폼과 필드들을 복사하여 새 매장에 할당
        Form defaultForm = formReader.readDefaultForm();
        Optional<Shop> shop = shopRepository.findById(shopId);
        Form newForm = Form.createForm(shop.orElseThrow(() -> new ShopException(ShopErrorCode.SHOP_NOT_FOUND)));
        Form savedForm = formRepository.save(newForm);

        List<FormField> defaultFields = formFieldReader.read(defaultForm);
        List<FormField> newFields = defaultFields.stream()
                .map(field -> FormField.copyFrom(field, savedForm))
                .collect(Collectors.toList());
        formFieldRepository.saveAll(newFields);
    }

}