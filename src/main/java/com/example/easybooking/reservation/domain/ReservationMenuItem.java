package com.example.easybooking.reservation.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
        name = "reservation_menu_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_reservation_menu_items_res_shop_menu",
                columnNames = {"reservation_id", "shop_menu_id"}
        ),
        indexes = {
                @Index(name = "idx_reservation_menu_items_reservation", columnList = "reservation_id, sort_order"),
                @Index(name = "idx_reservation_menu_items_shop_menu", columnList = "shop_menu_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "shop_menu_id", nullable = false)
    private Long shopMenuId;

    @Column(name = "menu_name_snapshot", nullable = false)
    private String menuNameSnapshot;

    @Column(name = "tag_name_snapshot")
    private String tagNameSnapshot;

    @Column(name = "price_snapshot")
    private Long priceSnapshot;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ReservationMenuItem create(Long reservationId, Long shopMenuId,
                                             String menuNameSnapshot, String tagNameSnapshot, Long priceSnapshot,
                                             int sortOrder) {
        ReservationMenuItem item = new ReservationMenuItem();
        item.reservationId = reservationId;
        item.shopMenuId = shopMenuId;
        item.menuNameSnapshot = menuNameSnapshot;
        item.tagNameSnapshot = tagNameSnapshot;
        item.priceSnapshot = priceSnapshot;
        item.sortOrder = sortOrder;
        return item;
    }
}
