package com.myce.ai.facade;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.service.ChatCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatDataFacade {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatCacheService chatCacheService;

    public boolean checkRoomIsWaitingAdmin(String roomCode){
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByRoomCode(roomCode);
        return chatRoom.map(ChatRoom::isWaitingForAdmin).orElse(false);
    }

    public List<ChatMessage> getRecentMessages(String roomCode){
        return chatMessageRepository
                .findTop50ByRoomCodeOrderBySentAtDesc(roomCode);
    }


    public ChatMessage saveChatMessage(String roomCode, Long senderId,
                                       String senderType, String senderName, String content) {
        ChatMessage aiMessage = ChatMessage.builder()
                .roomCode(roomCode)
                .senderType(senderType)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(aiMessage);

        // AI 메시지를 Redis 캐시에도 추가 (새로고침 후에도 보이도록)
        chatCacheService.addMessageToCache(roomCode, savedMessage);
        // 채팅방 마지막 메세지 업데이트
        updateChatRoomLastMessage(roomCode, savedMessage.getId(), content);

        return savedMessage;
    }

    public Optional<ChatRoom> getChatRoom(String roomCode) {
        return chatRoomRepository.findByRoomCode(roomCode);
    }

    public ChatRoom saveChatRoom(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    public void cacheChatRoom(String roomCode, ChatRoom chatRoom) {
        chatCacheService.cacheChatRoom(roomCode, chatRoom);
    }

    private void updateChatRoomLastMessage(String roomCode, String messageId, String content) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomCode(roomCode);

        if (chatRoomOpt.isPresent()) {
            ChatRoom chatRoom = chatRoomOpt.get();
            chatRoom.updateLastMessageInfo(messageId, content);
            chatRoomRepository.save(chatRoom);
        }
    }
}
