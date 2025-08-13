package com.myce.schedule.jobs;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.repository.ChatRoomRepository;
import com.myce.schedule.TaskScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 채팅 관리자 담당자 타임아웃 스케줄러
 * 일정 시간 비활성 상태인 담당자를 자동 해제하여 다른 관리자가 담당할 수 있도록 함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminTimeoutScheduler implements TaskScheduler {

    private final ChatRoomRepository chatRoomRepository;
    
    // 테스트 환경: 1분간 비활성시 해제 (원래 5분)
    private static final int TIMEOUT_MINUTES = 1;
    
    @PostConstruct
    public void init() {
        log.debug("[Scheduler] Registered admin timeout scheduler - 담당자 {}분 비활성시 자동 해제", TIMEOUT_MINUTES);
    }

    @Override
    @Scheduled(cron = "${scheduler.admin-timeout: 0 */1 * * * *}") // 매 1분마다 실행, 설정으로 변경 가능
    public void run() {
        try {
            this.process();
        } catch (Exception e) {
            log.error("담당자 타임아웃 스케줄러 실행 실패", e);
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
        List<ChatRoom> roomsToUpdate = new ArrayList<>();
        
        for (ChatRoom room : inactiveRooms) {
            String releasedAdmin = room.getCurrentAdminCode();
            String adminDisplayName = room.getAdminDisplayName();
            
            // 담당자 해제
            room.releaseAdmin();
            roomsToUpdate.add(room);
            
            log.info("비활성 담당자 해제 예정: [{}] {} ({}분간 비활성)", 
                    room.getRoomCode(), adminDisplayName, TIMEOUT_MINUTES);
        }
        
        // 한번에 saveAll
        chatRoomRepository.saveAll(roomsToUpdate);
        
        log.info("담당자 타임아웃 처리 완료: {}건 일괄 업데이트됨", roomsToUpdate.size());
    }
}