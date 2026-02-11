package com.example.easybooking.shop;

import com.example.easybooking.shop.domain.ShopMenuInputField;
import com.example.easybooking.shop.repository.ShopMenuInputFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopMenuInputFieldWriter {

    private final ShopMenuInputFieldRepository repository;

    public ShopMenuInputField save(ShopMenuInputField field) {
        return repository.save(field);
    }
}
