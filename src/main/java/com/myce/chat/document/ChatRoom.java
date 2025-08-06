package com.myce.chat.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 채팅방 MongoDB Document 엔티티
 * 
 * 컬렉션: chat_rooms
 * 
 * 핵심 개념:
 * 1. 1:1 채팅방 구조 (관리자 ↔ 참가자)
 * 2. 박람회별 채팅방 관리
 * 3. 마지막 메시지 추적으로 목록 정렬
 * 4. 읽지 않은 메시지 관리
 * 5. 채팅방 활성화/비활성화 상태 관리
 * 
 * 인덱스 전략:
 * - roomCode: 유니크 인덱스 (빠른 채팅방 검색)
 * - memberId + isActive: 사용자별 활성 채팅방 조회
 * - expoId + isActive: 박람회별 활성 채팅방 조회
 * - lastMessageAt: 최신 메시지 순 정렬용
 * 
 * @author MYCE Team
 * @since 2025-08-06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_rooms")
@CompoundIndexes({
    @CompoundIndex(name = "member_active_idx", def = "{'memberId': 1, 'isActive': 1, 'lastMessageAt': -1}"),
    @CompoundIndex(name = "expo_active_idx", def = "{'expoId': 1, 'isActive': 1, 'lastMessageAt': -1}"),
    @CompoundIndex(name = "expo_member_idx", def = "{'expoId': 1, 'memberId': 1}", unique = true)
})
public class ChatRoom {

    /**
     * MongoDB ObjectId (자동 생성)
     * 채팅방 고유 식별자
     * 
     * 형식: ObjectId("507f1f77bcf86cd799439011")
     * 사용: API 응답, WebSocket 구독 등에서 채팅방 식별
     */
    @Id
    private String id;

    /**
     * 채팅방 코드 (비즈니스 키)
     * 형식: "admin-{expoId}-{memberId}"
     * 
     * 예시:
     * - "admin-12345-67890" (박람회 12345, 참가자 67890)
     * - "admin-54321-11111" (박람회 54321, 참가자 11111)
     * 
     * 용도:
     * - WebSocket 채널 구독 시 사용
     * - 채팅방 중복 생성 방지
     * - 프론트엔드에서 채팅방 식별
     */
    @Indexed(unique = true)
    private String roomCode;

    /**
     * 참가자 회원 ID (MySQL Member 테이블 참조)
     * 
     * 비즈니스 로직:
     * - 박람회에 관심을 보인 일반 사용자
     * - 부스 예약이나 문의를 위해 관리자와 채팅
     * - 하나의 박람회당 하나의 채팅방만 생성 가능
     */
    @Indexed
    private Long memberId;

    /**
     * 참가자 이름 (캐시용)
     * 
     * 목적:
     * - 채팅방 목록에서 빠른 표시를 위한 비정규화
     * - Member 테이블 조회 없이 바로 이름 표시 가능
     * - 실시간 채팅에서 발송자 이름 표시
     * 
     * 주의사항:
     * - Member 테이블의 name이 변경되면 동기화 필요
     * - 정확한 정보는 Member 테이블을 우선으로 함
     */
    private String memberName;

    /**
     * 박람회 ID (MySQL Expo 테이블 참조)
     * 
     * 비즈니스 로직:
     * - 채팅 주제가 되는 박람회
     * - 박람회 관리자가 참가자들과 소통하는 공간
     * - 박람회별 채팅 통계 및 관리에 사용
     */
    @Indexed
    private Long expoId;

    /**
     * 박람회 제목 (캐시용)
     * 
     * 목적:
     * - 채팅방 목록에서 "어느 박람회" 채팅인지 빠른 식별
     * - Expo 테이블 조회 없이 바로 표시 가능
     * - 채팅 히스토리에서 컨텍스트 제공
     * 
     * 주의사항:
     * - Expo 테이블의 title이 변경되면 동기화 필요
     * - 정확한 정보는 Expo 테이블을 우선으로 함
     */
    private String expoTitle;

    /**
     * 채팅방 활성화 상태
     * 
     * true: 활성화 (정상 사용 가능)
     * false: 비활성화 (목록에서 숨김, 메시지 전송 차단)
     * 
     * 비활성화 시나리오:
     * - 박람회 종료 후 일정 시간 경과
     * - 스팸이나 부적절한 대화로 인한 관리자 차단
     * - 참가자가 채팅방 나가기 선택
     * - 시스템 정책에 의한 자동 비활성화
     */
    @Indexed
    private Boolean isActive;

    /**
     * 마지막 메시지 내용 (미리보기용)
     * 
     * 용도:
     * - 채팅방 목록에서 마지막 대화 미리보기 표시
     * - 사용자가 대화 내용을 빠르게 파악할 수 있도록 함
     * 
     * 제한사항:
     * - 최대 200자로 제한 (긴 메시지는 "..." 처리)
     * - 이미지/파일 메시지는 "[이미지]", "[파일]" 등으로 표시
     * - 시스템 메시지는 표시하지 않음
     */
    private String lastMessage;

    /**
     * 마지막 메시지 ID (ChatMessage 컬렉션 참조)
     * 
     * 용도:
     * - 채팅 히스토리 로딩 시 시작점 결정
     * - 읽지 않은 메시지 계산의 기준점
     * - 메시지 동기화 및 중복 방지
     */
    private String lastMessageId;

    /**
     * 마지막 메시지 전송 시간
     * 
     * 용도:
     * - 채팅방 목록 정렬 (최신 활동 순)
     * - "5분 전", "어제" 등 상대적 시간 표시
     * - 채팅방 활성도 측정 지표
     * - 자동 비활성화 정책의 기준 시간
     */
    @Indexed
    private LocalDateTime lastMessageAt;

    /**
     * 각 사용자별 마지막 읽은 메시지 정보 (JSON 형태)
     * 
     * 구조 예시:
     * {
     *   "12345": {                    // 관리자 ID
     *     "lastReadMessageId": "abc123",
     *     "lastReadAt": "2025-08-06T10:30:00"
     *   },
     *   "67890": {                    // 참가자 ID
     *     "lastReadMessageId": "def456", 
     *     "lastReadAt": "2025-08-06T10:25:00"
     *   }
     * }
     * 
     * 용도:
     * - 사용자별 읽지 않은 메시지 개수 계산
     * - 메시지 읽음 상태 표시 (상대방이 읽었는지)
     * - 읽지 않은 메시지 배지 표시
     */
    private String readStatusJson;

    /**
     * 채팅방 생성 시간
     * 
     * 용도:
     * - 채팅방 생성 순서 추적
     * - 오래된 채팅방 정리 정책 기준
     * - 시스템 통계 및 분석
     * - 감사 로그 (audit log)
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 채팅방 정보 최종 수정 시간
     * 
     * 용도:
     * - 채팅방 메타데이터 변경 추적
     * - 동기화 및 캐시 무효화
     * - 시스템 모니터링
     */
    @LastModifiedDate  
    private LocalDateTime updatedAt;

    /**
     * 채팅방 생성 시 기본값 설정
     * 
     * @param roomCode 채팅방 코드
     * @param memberId 참가자 ID
     * @param memberName 참가자 이름
     * @param expoId 박람회 ID
     * @param expoTitle 박람회 제목
     */
    @Builder
    public ChatRoom(String roomCode, Long memberId, String memberName, 
                   Long expoId, String expoTitle) {
        this.roomCode = roomCode;
        this.memberId = memberId;
        this.memberName = memberName;
        this.expoId = expoId;
        this.expoTitle = expoTitle;
        this.isActive = true;  // 기본값: 활성화
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.readStatusJson = "{}";  // 빈 JSON 객체로 초기화
    }

    /**
     * 새 메시지 발송 시 채팅방 정보 업데이트
     * 
     * @param messageId 새 메시지 ID
     * @param messageContent 메시지 내용 (미리보기용)
     */
    public void updateLastMessageInfo(String messageId, String messageContent) {
        this.lastMessageId = messageId;
        this.lastMessage = truncateMessage(messageContent);
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 채팅방 비활성화
     * 
     * 사용 시나리오:
     * - 박람회 종료
     * - 사용자 요청으로 채팅방 나가기
     * - 부적절한 대화로 인한 관리자 조치
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 채팅방 재활성화
     * 
     * 사용 시나리오:
     * - 박람회 기간 연장
     * - 실수로 비활성화된 채팅방 복구
     * - 차단 해제
     */
    public void reactivate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 메시지 내용 자르기 (미리보기용)
     * 
     * @param content 원본 메시지 내용
     * @return 200자로 제한된 메시지 (필요시 "..." 추가)
     */
    private String truncateMessage(String content) {
        if (content == null) return null;
        if (content.length() <= 200) return content;
        return content.substring(0, 197) + "...";
    }
}
