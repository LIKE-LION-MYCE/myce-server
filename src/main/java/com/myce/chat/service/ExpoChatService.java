package com.myce.chat.service;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.dto.MessageResponse;
import com.myce.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 박람회 관리자 채팅 서비스 인터페이스
 * 기존 Service + ServiceImpl 패턴 준수
 */
public interface ExpoChatService {

    /**
     * 관리자용 채팅방 목록 조회
     * @param expoId 박람회 ID
     * @param userDetails 인증된 사용자 정보 
     * @return 채팅방 목록
     */
    List<ChatRoomListResponse> getChatRoomsForAdmin(Long expoId, CustomUserDetails userDetails);

    /**
     * 채팅방 메시지 조회 (페이징)
     * @param expoId 박람회 ID
     * @param roomCode 채팅방 코드
     * @param pageable 페이징 정보
     * @param userDetails 인증된 사용자 정보
     * @return 메시지 목록
     */
    PageResponse<MessageResponse> getMessages(Long expoId, String roomCode, Pageable pageable, CustomUserDetails userDetails);

    /**
     * 메시지 읽음 처리
     * @param expoId 박람회 ID
     * @param roomCode 채팅방 코드
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @param userDetails 인증된 사용자 정보
     */
    void markAsRead(Long expoId, String roomCode, String lastReadMessageId, CustomUserDetails userDetails);

    /**
     * 안읽은 메시지 수 조회
     * @param expoId 박람회 ID
     * @param roomCode 채팅방 코드
     * @param userDetails 인증된 사용자 정보
     * @return 안읽은 메시지 수
     */
    Long getUnreadCount(Long expoId, String roomCode, CustomUserDetails userDetails);
    
    /**
     * 사용자용 전체 읽지 않은 메시지 수 조회 (FAB용)
     * @param userDetails 인증된 사용자 정보
     * @return 전체 읽지 않은 메시지 수 정보
     */
    Map<String, Object> getAllUnreadCountsForUser(CustomUserDetails userDetails);
}