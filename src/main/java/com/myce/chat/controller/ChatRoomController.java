package com.myce.chat.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.chat.dto.ChatRoomListResponse;
import com.myce.chat.service.ChatRoomService;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * CRM-186: 채팅방 목록 조회 API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 사용자별 채팅방 목록 조회 (USER: 본인 참여, ADMIN: 관리 박람회 전체)
     */
    @GetMapping
    public ResponseEntity<ChatRoomListResponse> getChatRooms(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        log.info("채팅방 목록 조회 API 호출 - 회원ID: {}, 역할: {}", 
                customUserDetails.getMemberId(), customUserDetails.getRole());

        ChatRoomListResponse response = chatRoomService.getChatRooms(
                customUserDetails.getMemberId(), 
                customUserDetails.getRole()
        );

        log.info("채팅방 목록 조회 성공 - 회원ID: {}, 조회된 채팅방 수: {}", 
                customUserDetails.getMemberId(), response.getTotalCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 박람회별 채팅방 목록 조회 (관리자 전용, 권한 검증 포함)
     */
    @GetMapping("/expo/{expoId}")
    public ResponseEntity<ChatRoomListResponse> getChatRoomsByExpo(
            @PathVariable("expoId") Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        
        log.info("박람회별 채팅방 조회 API 호출 - 박람회ID: {}, 관리자ID: {}", 
                expoId, customUserDetails.getMemberId());

        ChatRoomListResponse response = chatRoomService.getChatRoomsByExpo(
                expoId, 
                customUserDetails.getMemberId()
        );

        log.info("박람회별 채팅방 조회 성공 - 박람회ID: {}, 관리자ID: {}, 채팅방 수: {}", 
                expoId, customUserDetails.getMemberId(), response.getTotalCount());

        return ResponseEntity.ok(response);
    }
}