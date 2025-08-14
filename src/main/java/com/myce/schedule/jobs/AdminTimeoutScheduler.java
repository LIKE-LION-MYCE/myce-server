package com.myce.schedule.jobs;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.dto.WebSocketMessage;
import com.myce.chat.repository.ChatRoomRepository;
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
 * 채팅 관리자 담당자 타임아웃 스케줄러
 * 일정 시간 비활성 상태인 담당자를 자동 해제하여 다른 관리자가 담당할 수 있도록 함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminTimeoutScheduler implements TaskScheduler {

    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // 테스트 환경: 1분간 비활성시 해제 (원래 5분)
    private static final int TIMEOUT_MINUTES = 1;
    
    @PostConstruct
    public void init() {
        log.debug("[Scheduler] Registered admin timeout scheduler - Auto release after {} minutes of inactivity", TIMEOUT_MINUTES);
    }

    @Override
    @Scheduled(cron = "${scheduler.admin-timeout: 0 */1 * * * *}") // 매 1분마다 실행, 설정으로 변경 가능
    public void run() {
        try {
            this.process();
        } catch (Exception e) {
            log.error("Admin timeout scheduler execution failed", e);
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
        
        // List로 모아서 일괄 처리
        List<ChatRoom> rooms = new ArrayList<>();
        
        for (ChatRoom room : inactiveRooms) {
            String releasedAdmin = room.getCurrentAdminCode();
            String adminDisplayName = room.getAdminDisplayName();
            
            // 담당자 해제
            room.releaseAdmin();
            rooms.add(room);
            
            log.info("Releasing inactive admin: [{}] {} ({} minutes inactive)", 
                    room.getRoomCode(), adminDisplayName, TIMEOUT_MINUTES);
        }
        
        // 한번에 saveAll
        chatRoomRepository.saveAll(rooms);
        
        // 배치로 WebSocket 알림 전송 (엑스포별 그룹핑)
        if (!rooms.isEmpty()) {
            sendBatchReleaseNotifications(rooms);
        }
        
        log.info("Admin timeout processing completed: {} rooms batch updated", rooms.size());
    }
    
    /**
     * 담당자 해제 알림을 배치로 전송
     * 엑스포별로 그룹핑하여 효율적으로 전송
     * 
     * @param releasedRooms 해제된 채팅방 목록
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