package com.myce.schedule.jobs;

import com.myce.chat.document.ChatMessage;
import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.WebSocketMessage;
import com.myce.chat.repository.ChatMessageRepository;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.chat.type.MessageSenderType;
import com.myce.chat.type.WebSocketMessageType;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 채팅 관리자 타임아웃 스케줄러 (하이브리드 시스템 백업)
 * - Platform: 10분 비활성시 AI로 자동 전환 (고급 AI 인계 로직)
 * - Expo: 10분 비활성시 단순 해제 (향후 수동 제어 시스템의 백업용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminTimeoutScheduler implements TaskScheduler {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // 하이브리드 백업 시스템: 10분간 비활성시 자동 처리
    private static final int TIMEOUT_MINUTES = 10;
    
    @PostConstruct
    public void init() {
        log.debug("[Scheduler] Registered hybrid admin timeout scheduler - {}분 비활성시 자동 처리 (platform→AI, expo→release)", TIMEOUT_MINUTES);
    }

    @Override
    @Scheduled(cron = "${scheduler.admin-timeout}")
    public void run() {
        try {
            this.process();
        } catch (Exception e) {
            log.error("Error occurred during admin timeout scheduler execution", e);
        }
    }

    @Override
    @Transactional
    public void process() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        
        // 비활성 담당자가 있는 채팅방 조회
        List<ChatRoom> inactiveRooms = chatRoomRepository
                .findByCurrentAdminCodeIsNotNullAndLastAdminActivityBefore(threshold);
        
        if (inactiveRooms.isEmpty()) {
            return;
        }
        
        // 분류하여 처리: expo admin vs platform admin
        List<ChatRoom> expoRoomsToUpdate = new ArrayList<>();
        List<ChatRoom> platformRoomsToProcess = new ArrayList<>();
        
        for (ChatRoom room : inactiveRooms) {
            if (room.getRoomCode().startsWith("platform-")) {
                platformRoomsToProcess.add(room);
            } else {
                // 기존 expo admin 로직: 단순 해제
                String releasedAdmin = room.getCurrentAdminCode();
                String adminDisplayName = room.getAdminDisplayName();
                
                room.releaseAdmin();
                expoRoomsToUpdate.add(room);
                
                log.info("비활성 expo 담당자 해제: [{}] {} ({}분간 비활성)", 
                        room.getRoomCode(), adminDisplayName, TIMEOUT_MINUTES);
            }
        }
        
        // Expo rooms: 기존 로직 (단순 해제) + 배치 알림
        if (!expoRoomsToUpdate.isEmpty()) {
            chatRoomRepository.saveAll(expoRoomsToUpdate);
            sendBatchReleaseNotifications(expoRoomsToUpdate); // 추가: 배치 알림 전송
            log.info("Expo 담당자 타임아웃 처리: {}건 해제됨 (배치 알림 포함)", expoRoomsToUpdate.size());
        }
        
        // Platform rooms: AI 전환 로직
        for (ChatRoom platformRoom : platformRoomsToProcess) {
            processPlatformRoomTimeout(platformRoom);
        }
        
        if (!platformRoomsToProcess.isEmpty()) {
            log.info("Platform 담당자 타임아웃 처리: {}건 AI로 전환됨", platformRoomsToProcess.size());
        }
    }
    
    /**
     * 플랫폼 관리자 타임아웃 처리 (AI로 자동 전환)
     */
    private void processPlatformRoomTimeout(ChatRoom room) {
        try {
            String releasedAdminCode = room.getCurrentAdminCode();
            String adminDisplayName = room.getAdminDisplayName();
            String roomCode = room.getRoomCode();
            
            log.info("Platform 관리자 타임아웃 처리: [{}] {} ({}분간 비활성) → AI 전환", 
                roomCode, adminDisplayName, TIMEOUT_MINUTES);
            
            // 1. AI 전환 알림 메시지 생성 및 저장
            ChatMessage timeoutMessage = ChatMessage.builder()
                .roomCode(roomCode)
                .senderType(MessageSenderType.AI.name())
                .senderId(-1L)  // AI system uses ID -1L
                .senderName("AI 상담사")
                .content(String.format("🔄 %s님이 자리를 비워 AI가 상담을 이어받았습니다. 계속 도움을 드리겠습니다!", 
                    adminDisplayName != null ? adminDisplayName : "상담원"))
                .build();
            
            ChatMessage savedMessage = chatMessageRepository.save(timeoutMessage);
            
            // 2. 관리자 해제 (AI_ACTIVE 상태로 전환)
            room.releaseAdmin();
            ChatRoom savedRoom = chatRoomRepository.save(room);
            
            // 3. 상태 정보 생성
            Map<String, Object> roomState = createRoomStateInfo(savedRoom, "admin_timeout");
            
            // 4. AI 전환 메시지 WebSocket 브로드캐스트
            Map<String, Object> messagePayload = Map.of(
                "roomCode", roomCode,
                "messageId", savedMessage.getId(),
                "senderId", savedMessage.getSenderId(),
                "senderType", savedMessage.getSenderType(),
                "senderName", savedMessage.getSenderName(),
                "content", savedMessage.getContent(),
                "sentAt", savedMessage.getSentAt().toString()
            );
            
            Map<String, Object> broadcastMessage = Map.of(
                "type", "AI_TIMEOUT_TAKEOVER",
                "payload", messagePayload,
                "roomState", roomState
            );
            
            messagingTemplate.convertAndSend("/topic/chat/" + roomCode, broadcastMessage);
            
            // 5. 버튼 상태 업데이트
            Map<String, Object> buttonPayload = Map.of(
                "roomId", roomCode,
                "state", "AI_ACTIVE",
                "buttonText", "Request Human",
                "buttonAction", "request_handoff"
            );
            
            Map<String, Object> buttonBroadcast = Map.of(
                "type", "BUTTON_STATE_UPDATE",
                "payload", buttonPayload,
                "roomState", roomState
            );
            
            messagingTemplate.convertAndSend("/topic/chat/" + roomCode, buttonBroadcast);
            
            log.info("✅ Platform 관리자 타임아웃 처리 완료: [{}] {} → AI_ACTIVE", roomCode, adminDisplayName);
            
        } catch (Exception e) {
            log.error("Platform 관리자 타임아웃 처리 실패: [{}]", room.getRoomCode(), e);
        }
    }
    
    /**
     * 채팅방 상태 정보 생성
     */
    private Map<String, Object> createRoomStateInfo(ChatRoom chatRoom, String transitionReason) {
        ChatRoom.ChatRoomState currentState = chatRoom.getCurrentState();
        return Map.of(
            "current", currentState.name(),
            "description", currentState.getDescription(),
            "buttonText", currentState.getButtonText(),
            "timestamp", LocalDateTime.now().toString(),
            "transitionReason", transitionReason
        );
    }
    
    /**
     * 엑스포 담당자 해제 알림을 배치로 전송
     * 엑스포별로 그룹핑하여 효율적으로 전송
     */
    private void sendBatchReleaseNotifications(List<ChatRoom> releasedRooms) {
        // 엑스포별로 채팅방 코드 그룹핑
        Map<Long, List<String>> expoRoomCodes = releasedRooms.stream()
            .collect(Collectors.groupingBy(
                ChatRoom::getExpoId,
                Collectors.mapping(ChatRoom::getRoomCode, Collectors.toList())
            ));
        
        // 각 엑스포별로 배치 메시지 전송
        for (Map.Entry<Long, List<String>> entry : expoRoomCodes.entrySet()) {
            Long expoId = entry.getKey();
            List<String> roomCodes = entry.getValue();
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("roomCodes", roomCodes);
            payload.put("message", "담당자가 자동 해제되었습니다.");
            payload.put("timestamp", LocalDateTime.now());
            
            WebSocketMessage batchMessage = WebSocketMessage.builder()
                    .type(WebSocketMessageType.ADMIN_RELEASED)
                    .payload(payload)
                    .build();
            
            // 해당 엑스포의 관리자들에게 배치 전송
            String destination = "/topic/expo/" + expoId + "/admin-updates";
            messagingTemplate.convertAndSend(destination, batchMessage);
        }
    }
}