package com.example.easybooking.user.service;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.repository.ChatRoomRepository;
import com.example.easybooking.chat.repository.MessageRepository;
import com.example.easybooking.form.domain.repository.FormFieldRepository;
import com.example.easybooking.form.domain.repository.FormRepository;
import com.example.easybooking.reservation.domain.repository.ReservationRepository;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.shop.repository.ShopRepository;
import com.example.easybooking.slot.SlotRepository;
import com.example.easybooking.user.domain.repository.UserRepository;
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
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final SlotRepository slotRepository;
    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;

    /**
     * 회원을 강제로 삭제하며, 연관된 모든 데이터를 정리합니다.
     * 주의: 이 작업은 되돌릴 수 없습니다.
     */
    @Transactional
    public void forceDeleteUser(Long userId) {
        // 1. 사용자가 소유한 샵(Shop)과 하위 리소스 정리 (원장님일 경우)
        List<Shop> myShops = shopRepository.findAllByOwnerId(userId);
        
        for (Shop shop : myShops) {
            Long shopId = shop.getId();

            // 1-1. 샵 관련 예약 삭제
            reservationRepository.deleteByShopId(shopId);

            // 1-2. 샵 관련 채팅방 및 메시지 삭제
            List<ChatRoom> shopRooms = chatRoomRepository.findByShopId(shopId);
            for(ChatRoom room : shopRooms) {
                messageRepository.deleteByChatRoomId(room.getId());
            }
            chatRoomRepository.deleteByShopId(shopId);

            // 1-3. 샵 관련 폼(Form) 및 필드 삭제
            formRepository.findByShopId(shopId).ifPresent(form -> {
                formFieldRepository.deleteByForm(form);
                formRepository.delete(form);
            });

            // 1-4. 샵 관련 슬롯 삭제
            slotRepository.deleteByShopId(shopId);
        }

        // 1-5. 샵 자체 삭제
        shopRepository.deleteByOwnerId(userId);


        // 2. 사용자가 참여한 활동 정리 (고객일 경우 및 원장님으로서의 잔여 데이터)
        
        // 2-1. 예약 내역 삭제 (고객으로서 한 예약)
        reservationRepository.deleteByCustomerId(userId);

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
}



