package com.example.easybooking.shop.service;

import com.example.easybooking.shop.ShopWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopWriter shopwriter;

}
