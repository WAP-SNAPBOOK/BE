package com.example.easybooking.shop.repository;

import com.example.easybooking.shop.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findAllByOwnerId(Long ownerId);

    boolean existsByIdAndOwnerId(Long shopId, Long ownerId);

    Optional<Shop> findByPublicCode(String publicCode);
    Optional<Shop> findBySlug(String slug);
    Optional<Shop> findByOwnerId(Long ownerId);
    boolean existsByOwnerId(Long ownerId);
}
