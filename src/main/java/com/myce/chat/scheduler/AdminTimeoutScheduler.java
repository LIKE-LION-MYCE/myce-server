package com.myce.chat.scheduler;

import com.myce.chat.document.ChatRoom;
import com.myce.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 담당자 타임아웃 스케줄러
 * 5분간 비활성 상태인 담당자를 자동 해제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminTimeoutScheduler {

    private final ChatRoomRepository chatRoomRepository;

    /**
     * 5분간 비활성 담당자 해제 (1분마다 실행)
     * 테스트용: 30초간 비활성 담당자 해제 (10초마다 실행)
     */
    @Scheduled(fixedRate = 10000) // 테스트용: 10초 = 10,000ms (원래: 60000)
    @Transactional
    public void releaseInactiveAdmins() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(30); // 테스트용: 30초 (원래: minusMinutes(5))
        
        // 5분간 비활성 담당자 조회
        List<ChatRoom> inactiveRooms = chatRoomRepository
                .findByCurrentAdminCodeIsNotNullAndLastAdminActivityBefore(threshold);
        
        if (inactiveRooms.isEmpty()) {
            log.debug("담당자 타임아웃 체크: 비활성 담당자 없음");
            return;
        }
        
        for (ChatRoom room : inactiveRooms) {
            String releasedAdmin = room.getCurrentAdminCode();
            
            // 담당자 해제
            room.releaseAdmin();
            chatRoomRepository.save(room);
            
            log.info("비활성 담당자 해제: {} -> {} (30초간 비활성)", 
                    room.getRoomCode(), releasedAdmin);
        }
        
        log.info("담당자 타임아웃 처리 완료: {}건 해제됨", inactiveRooms.size());
    }
}