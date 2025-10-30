package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByPublicCode(String publicCode);
    Optional<Shop> findBySlug(String slug);
    boolean existsByOwnerId(Long ownerId);
}
