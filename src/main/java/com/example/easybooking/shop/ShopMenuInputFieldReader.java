package com.example.easybooking.shop;

import com.example.easybooking.shop.domain.ShopMenuInputField;
import com.example.easybooking.shop.repository.ShopMenuInputFieldRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopMenuInputFieldReader {

    private final ShopMenuInputFieldRepository repository;

    public List<ShopMenuInputField> findActiveByMenuId(Long shopMenuId) {
        return repository.findByShopMenuIdAndIsActiveTrueOrderBySortOrderAsc(shopMenuId);
    }

    public ShopMenuInputField getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ShopMenuInputField not found: " + id));
    }
}
