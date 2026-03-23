package com.example.easybooking.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "shop_menu_tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_shop_menu_tags",
                        columnNames = {"shop_menu_id", "tag_id"}
                ),
                @UniqueConstraint(
                        name = "uq_shop_menu_tags_shop_tag",
                        columnNames = {"shop_menu_id", "shop_tag_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopMenuTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_menu_id", nullable = false)
    private Long shopMenuId;

    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "shop_tag_id")
    private Long shopTagId;

    public static ShopMenuTag create(Long shopMenuId, Long tagId) {
        ShopMenuTag tag = new ShopMenuTag();
        tag.shopMenuId = shopMenuId;
        tag.tagId = tagId;
        return tag;
    }

    public static ShopMenuTag createResolved(Long shopMenuId, Long tagId, Long shopTagId) {
        ShopMenuTag tag = new ShopMenuTag();
        tag.shopMenuId = shopMenuId;
        tag.tagId = tagId;
        tag.shopTagId = shopTagId;
        return tag;
    }
}
