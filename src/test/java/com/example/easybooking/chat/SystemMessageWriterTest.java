package com.example.easybooking.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.easybooking.chat.domain.ChatRoom;
import com.example.easybooking.chat.domain.Message;
import com.example.easybooking.chat.domain.MessageType;
import com.example.easybooking.chat.dto.response.MessageResponse;
import com.example.easybooking.chat.repository.MessageRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SystemMessageWriterTest {

    @Mock MessageRepository messageRepository;
    @Mock ChatRoomReader chatRoomReader;

    @Test
    void saveReservationMessage_includesDurationMinutes() {
        long chatRoomId = 100L;
        long reservationId = 200L;

        ChatRoom chatRoom = mock(ChatRoom.class);
        when(chatRoomReader.read(chatRoomId)).thenReturn(chatRoom);

        SystemMessageWriter writer = new SystemMessageWriter(messageRepository, chatRoomReader);

        when(messageRepository.save(org.mockito.ArgumentMatchers.any(Message.class)))
                .thenAnswer(invocation -> {
                    Message message = invocation.getArgument(0);
                    ReflectionTestUtils.setField(message, "id", 999L);
                    return message;
                });

        MessageResponse response = writer.saveReservationMessage(
                chatRoomId,
                reservationId,
                75,
                MessageType.RESERVATION_CONFIRMED
        );

        assertThat(response.getDurationMinutes()).isEqualTo(75);
        assertThat(response.getReservationId()).isEqualTo(reservationId);
        assertThat(response.getMessageType()).isEqualTo(MessageType.RESERVATION_CONFIRMED);
    }
}
