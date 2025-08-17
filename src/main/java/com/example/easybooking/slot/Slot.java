package com.example.easybooking.slot;

import com.example.easybooking.shop.Shop;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 각 시간 슬롯이 고유 ID를 가짐

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(nullable = false)
    private LocalDateTime startDateTime; // 날짜와 시간을 함께 가짐

    @Column(nullable = false)
    private boolean isBooked = false; // '예약 가능/불가' 상태를 독립적으로 가짐

    static Slot create(Shop shop, LocalDateTime startDateTime) {
        Slot slot = new Slot();
        slot.shop = shop;
        slot.startDateTime = startDateTime;
        return slot;
    }
}
