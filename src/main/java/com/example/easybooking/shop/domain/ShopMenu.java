package com.example.easybooking.shop.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "shop_menus",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_shop_menus_shop_name",
                columnNames = {"shop_id", "name"}
        ),
        indexes = {
                @Index(name = "idx_shop_menus_shop_active_sort", columnList = "shop_id, is_active, sort_order")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "price")
    private Long price;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static ShopMenu create(Long shopId, String name, String description,
                                  Boolean isActive, Integer sortOrder) {
        return create(shopId, name, description, null, isActive, sortOrder);
    }

    public static ShopMenu create(Long shopId, String name, String description, Long price,
                                  Boolean isActive, Integer sortOrder) {
        ShopMenu menu = new ShopMenu();
        menu.shopId = shopId;
        menu.name = name;
        menu.description = description;
        menu.price = price;
        menu.isActive = isActive;
        menu.sortOrder = sortOrder;
        return menu;
    }

    public void update(String name, String description, Integer sortOrder) {
        update(name, description, null, sortOrder);
    }

    public void update(String name, String description, Long price, Integer sortOrder) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            this.price = price;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
    }

    public void deactivate() {
        this.isActive = false;
    }
}
