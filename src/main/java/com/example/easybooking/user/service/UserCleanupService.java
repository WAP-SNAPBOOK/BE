package com.example.easybooking.user.service;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.availability.domain.ShopHoliday;
import com.example.easybooking.availability.repository.ShopHolidayRepository;
import com.example.easybooking.availability.repository.ShopOperatingTimeRepository;
import com.example.easybooking.availability.repository.ShopSettingsRepository;
import com.example.easybooking.availability.repository.StaffOperatingTimeRepository;
import com.example.easybooking.form.domain.repository.FormFieldRepository;
import com.example.easybooking.form.domain.repository.FormRepository;
import com.example.easybooking.reservation.domain.Reservation;
import com.example.easybooking.reservation.domain.ReservationMenuItem;
import com.example.easybooking.reservation.domain.repository.ReservationMenuInputValueRepository;
import com.example.easybooking.reservation.domain.repository.ReservationMenuItemRepository;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import com.example.easybooking.reservation.domain.repository.ReservationTimeBlockRepository;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.domain.ShopMenu;
import com.example.easybooking.shop.domain.ShopTag;
import com.example.easybooking.shop.repository.ShopMenuInputFieldRepository;
import com.example.easybooking.shop.repository.ShopMenuRepository;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.shop.repository.ShopTagRepository;
import com.example.easybooking.slot.SlotRepository;
import com.example.easybooking.staff.domain.Staff;
import com.example.easybooking.staff.repository.StaffRepository;
import com.example.easybooking.user.domain.repository.UserRepository;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMenuItemRepository reservationMenuItemRepository;
    private final ReservationMenuInputValueRepository reservationMenuInputValueRepository;
    private final ReservationTimeBlockRepository reservationTimeBlockRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final SlotRepository slotRepository;
    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;
    private final ShopMenuRepository shopMenuRepository;
    private final ShopMenuTagRepository shopMenuTagRepository;
    private final ShopMenuInputFieldRepository shopMenuInputFieldRepository;
    private final ShopTagRepository shopTagRepository;
    private final ShopSettingsRepository shopSettingsRepository;
    private final ShopOperatingTimeRepository shopOperatingTimeRepository;
    private final ShopHolidayRepository shopHolidayRepository;
    private final StaffRepository staffRepository;
    private final StaffOperatingTimeRepository staffOperatingTimeRepository;

    /**
     * 회원을 강제로 삭제하며, 연관된 모든 데이터를 정리합니다.
     * 주의: 이 작업은 되돌릴 수 없습니다.
     */
    @Transactional
    public void forceDeleteUser(Long userId) {
        // 1. 사용자가 소유한 샵(Shop)과 하위 리소스 정리 (원장님일 경우)
        List<Shop> myShops = shopRepository.findAllByOwnerId(userId);

        for (Shop shop : myShops) {
            deleteOwnedShopResources(shop.getId());
        }

        // 1-5. 샵 자체 삭제
        shopRepository.deleteByOwnerId(userId);


        // 2. 사용자가 참여한 활동 정리 (고객일 경우 및 원장님으로서의 잔여 데이터)
        
        // 2-1. 예약 내역 삭제 (고객으로서 한 예약)
        deleteReservations(reservationRepository.findByCustomerIdOrderByCreatedAtDesc(userId));

        // 2-2. 채팅방 정리 (남은 참여 방들)
        List<ChatRoom> remainingRooms = chatRoomRepository.findChatRooms(userId);
        for (ChatRoom room : remainingRooms) {
             messageRepository.deleteByChatRoomId(room.getId());
             chatRoomRepository.delete(room);
        }
        
        // 2-3. 사용자가 보낸 메시지 삭제 (혹시 모를 잔여 데이터)
        messageRepository.deleteBySenderId(userId);


        // 3. 마지막으로 사용자 삭제
        userRepository.deleteById(userId);
    }

    private void deleteOwnedShopResources(Long shopId) {
        deleteReservations(reservationRepository.findByShopId(shopId));
        deleteShopChatRooms(shopId);
        deleteShopForm(shopId);
        slotRepository.deleteByShopId(shopId);
        deleteShopMenus(shopId);
        deleteShopAvailability(shopId);
        deleteShopStaff(shopId);
    }

    private void deleteShopChatRooms(Long shopId) {
        List<ChatRoom> shopRooms = chatRoomRepository.findByShopId(shopId);
        for (ChatRoom room : shopRooms) {
            messageRepository.deleteByChatRoomId(room.getId());
        }
        chatRoomRepository.deleteByShopId(shopId);
    }

    private void deleteShopForm(Long shopId) {
        formRepository.findByShopId(shopId).ifPresent(form -> {
            formFieldRepository.deleteByForm(form);
            formRepository.delete(form);
        });
    }

    private void deleteShopMenus(Long shopId) {
        List<ShopMenu> shopMenus = shopMenuRepository.findByShopId(shopId);
        if (!shopMenus.isEmpty()) {
            List<Long> menuIds = shopMenus.stream()
                    .map(ShopMenu::getId)
                    .toList();
            shopMenuTagRepository.deleteByShopMenuIdIn(menuIds);
            shopMenuInputFieldRepository.deleteByShopMenuIdIn(menuIds);
            shopMenuRepository.deleteAll(shopMenus);
        }

        List<ShopTag> shopTags = shopTagRepository.findByShopIdOrderBySortOrderAsc(shopId);
        if (!shopTags.isEmpty()) {
            shopTagRepository.deleteAll(shopTags);
        }
    }

    private void deleteShopAvailability(Long shopId) {
        List<ShopHoliday> holidays = shopHolidayRepository.findByShopId(shopId);
        if (!holidays.isEmpty()) {
            shopHolidayRepository.deleteAll(holidays);
        }

        shopOperatingTimeRepository.deleteByShopId(shopId);

        shopSettingsRepository.findByShopId(shopId)
                .ifPresent(shopSettingsRepository::delete);
    }

    private void deleteShopStaff(Long shopId) {
        List<Staff> staffs = staffRepository.findByShopId(shopId);
        if (staffs.isEmpty()) {
            return;
        }

        for (Staff staff : staffs) {
            staffOperatingTimeRepository.deleteByStaffId(staff.getId());
        }
        staffRepository.deleteAll(staffs);
    }

    private void deleteReservations(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return;
        }

        List<Long> reservationIds = reservations.stream()
                .map(Reservation::getId)
                .toList();

        List<ReservationMenuItem> menuItems = reservationMenuItemRepository.findByReservationIdIn(reservationIds);
        if (!menuItems.isEmpty()) {
            List<Long> menuItemIds = new ArrayList<>(menuItems.size());
            for (ReservationMenuItem menuItem : menuItems) {
                menuItemIds.add(menuItem.getId());
            }
            reservationMenuInputValueRepository.deleteByReservationMenuItemIdIn(menuItemIds);
            reservationMenuItemRepository.deleteByReservationIdIn(reservationIds);
        }

        reservationTimeBlockRepository.deleteByReservationIdIn(reservationIds);
        reservationRepository.deleteAll(reservations);
    }
}



