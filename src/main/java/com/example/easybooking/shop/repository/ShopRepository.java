package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findAllByOwnerId(Long ownerId);

    boolean existsByOwnerId(Long ownerId);
}
