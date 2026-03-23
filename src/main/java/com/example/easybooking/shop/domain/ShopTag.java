package com.example.easybooking.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "shop_tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_shop_tags_shop_name",
                        columnNames = {"shop_id", "name"}
                ),
                @UniqueConstraint(
                        name = "uq_shop_tags_shop_sort",
                        columnNames = {"shop_id", "sort_order"}
                )
        },
        indexes = {
                @Index(name = "idx_shop_tags_shop_sort", columnList = "shop_id, sort_order")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static ShopTag create(Long shopId, String name, Integer sortOrder) {
        ShopTag tag = new ShopTag();
        tag.shopId = shopId;
        tag.name = name;
        tag.sortOrder = sortOrder;
        return tag;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
