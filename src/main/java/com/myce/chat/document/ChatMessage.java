package com.myce.chat.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 MongoDB Document 엔티티
 * 
 * 컬렉션: chat_messages
 * 
 * 핵심 개념:
 * 1. 실제 채팅 메시지 내용 저장
 * 2. 메시지 타입별 처리 (텍스트, 이미지, 파일, 시스템)
 * 3. 발송자/수신자 정보 관리
 * 4. 메시지 읽음 상태 추적
 * 5. 메시지 편집/삭제 히스토리
 * 
 * 인덱스 전략:
 * - roomCode + sentAt: 채팅방별 메시지 시간순 조회 (CRM-187용)
 * - senderId + sentAt: 발송자별 메시지 이력 조회
 * - sentAt: 전체 메시지 시간순 정렬
 * 
 * @author MYCE Team
 * @since 2025-08-06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_messages")
@CompoundIndexes({
    @CompoundIndex(name = "room_time_idx", def = "{'roomCode': 1, 'sentAt': -1}"),
    @CompoundIndex(name = "sender_time_idx", def = "{'senderId': 1, 'sentAt': -1}")
})
public class ChatMessage {

    /**
     * MongoDB ObjectId (자동 생성)
     * 메시지 고유 식별자
     * 
     * 용도:
     * - 메시지 수정/삭제 시 참조
     * - 읽음 상태 추적
     * - ChatRoom의 lastMessageId로 참조
     * - CRM-187 채팅 히스토리에서 페이징 기준
     */
    @Id
    private String id;

    /**
     * 채팅방 코드 (ChatRoom의 roomCode와 동일)
     * 형식: "admin-{expoId}-{memberId}"
     * 
     * 용도:
     * - CRM-187: 특정 채팅방의 메시지 히스토리 조회
     * - 메시지가 속한 채팅방 식별  
     * - WebSocket 브로드캐스팅 채널 결정
     * 
     * 예시:
     * - "admin-12345-67890" (박람회 12345, 참가자 67890)
     * - "admin-54321-11111" (박람회 54321, 참가자 11111)
     * 
     * 주의사항:
     * - 기존 chatRoomId (ObjectId)에서 roomCode (문자열)로 변경
     * - ChatRoom Document와 일관성 유지
     */
    @Indexed
    private String roomCode;

    /**
     * 발송자 타입 (기존 senderType 유지)
     * 
     * 값 종류:
     * - "ADMIN": 박람회 관리자
     * - "USER": 일반 참가자
     * - "SYSTEM": 시스템 메시지
     * 
     * 용도:
     * - UI에서 메시지 스타일링 (색상, 위치, 아이콘)
     * - 권한 기반 메시지 처리
     * - 통계 및 분석 데이터
     * - CRM-187에서 발송자별 필터링
     */
    private String senderType;

    /**
     * 발송자 회원 ID
     * 
     * 값 종류:
     * - 양수: 실제 회원 ID (관리자 또는 참가자)
     * - 0: 시스템 메시지 (입장/퇴장 알림, 자동 안내 등)
     * 
     * 비즈니스 로직:
     * - 관리자가 보낸 메시지: expo.member.id
     * - 참가자가 보낸 메시지: chatRoom.memberId
     * - 시스템 메시지: 0 (특별 처리)
     */
    @Indexed
    private Long senderId;

    /**
     * 발송자 이름 (캐시용)
     * 
     * 목적:
     * - 채팅 UI에서 빠른 발송자 표시
     * - Member 테이블 조회 없이 바로 사용
     * - 메시지 히스토리에서 컨텍스트 제공
     * - CRM-187 채팅 히스토리에서 발송자 표시
     * 
     * 특수값:
     * - "시스템": senderId가 0인 경우
     * - 실제 회원 이름: Member.name의 캐시
     * 
     * 주의사항:
     * - Member 테이블의 name이 변경되면 동기화 필요
     * - 정확한 정보는 Member 테이블을 우선으로 함
     */
    private String senderName;

    /**
     * 메시지 내용
     * 
     * 타입별 내용:
     * - TEXT: 실제 채팅 메시지 텍스트
     * - IMAGE: 이미지 파일명 또는 설명 ("[이미지]")
     * - FILE: 파일명 또는 설명 ("[파일]")
     * - SYSTEM: 시스템 알림 메시지
     * 
     * 제한사항:
     * - 최대 1000자 제한
     * - HTML 태그 필터링 적용
     * - 악성 스크립트 방지 처리
     * 
     * CRM-186 연동:
     * - ChatRoom.lastMessage 필드에 복사됨
     * - 200자로 잘려서 미리보기로 표시
     */
    private String content;

    /**
     * 메시지 전송 시간 (기존 sentAt 유지)
     * 
     * 용도:
     * - CRM-187: 메시지 시간순 정렬 및 페이징
     * - "5분 전", "어제" 등 상대적 시간 표시
     * - ChatRoom.lastMessageAt 업데이트
     * - 메시지 편집/삭제 시간 제한 계산
     */
    @CreatedDate
    @Indexed
    private LocalDateTime sentAt;

    /**
     * 시스템 메시지 여부 (기존 isSystemMessage 유지)
     * 
     * true: 시스템에서 자동 생성된 메시지
     * false: 사용자가 직접 작성한 메시지
     * 
     * 시스템 메시지 예시:
     * - "홍길동님이 채팅방에 입장하셨습니다."
     * - "박람회가 종료되었습니다."
     * - "파일을 업로드했습니다."
     * 
     * UI 처리:
     * - 시스템 메시지는 중앙 정렬, 회색 표시
     * - 일반 메시지는 발송자별 좌/우 정렬
     */
    private Boolean isSystemMessage;

    /**
     * 메시지 타입 (추가 필드)
     * 
     * 값 종류:
     * - "TEXT": 일반 텍스트 메시지
     * - "IMAGE": 이미지 파일
     * - "FILE": 첨부파일 (PDF, DOC 등)
     * - "SYSTEM_ENTER": 입장 알림
     * - "SYSTEM_LEAVE": 퇴장 알림
     * - "SYSTEM_INFO": 정보성 시스템 메시지
     * 
     * 처리 방식:
     * - TEXT: content 필드에 메시지 내용
     * - IMAGE/FILE: fileInfoJson 필드에 파일 정보
     * - SYSTEM_*: content에 시스템 메시지 내용
     */
    private String messageType;

    /**
     * 파일 정보 (JSON 형태) - messageType이 IMAGE 또는 FILE인 경우 사용
     * 
     * 구조 예시:
     * {
     *   "originalFileName": "presentation.pdf",
     *   "storedFileName": "20250806_abc123.pdf",
     *   "fileSize": 2048576,
     *   "mimeType": "application/pdf",
     *   "downloadUrl": "/api/files/download/abc123",
     *   "thumbnailUrl": "/api/files/thumbnail/abc123"
     * }
     * 
     * 용도:
     * - 파일 다운로드 링크 생성
     * - 파일 크기 및 타입 표시
     * - 이미지 미리보기 제공
     * - CRM-189 실시간 파일 전송에 사용
     */
    private String fileInfoJson;

    /**
     * 메시지 읽음 상태 (JSON 형태) - CRM-188 읽지 않은 메시지 개수 계산용
     * 
     * 구조 예시:
     * {
     *   "12345": {                    // 관리자 ID
     *     "isRead": true,
     *     "readAt": "2025-08-06T10:30:00"
     *   },
     *   "67890": {                    // 참가자 ID
     *     "isRead": false,
     *     "readAt": null
     *   }
     * }
     * 
     * 용도:
     * - CRM-188: 읽지 않은 메시지 개수 계산
     * - 메시지 읽음 표시 (더블체크 등)
     * - 읽음 상태 실시간 알림
     */
    private String readStatusJson;

    /**
     * 메시지 편집 여부
     * 
     * true: 메시지가 수정됨 (UI에서 "(편집됨)" 표시)
     * false: 원본 메시지 그대로
     * 
     * 편집 규칙:
     * - 텍스트 메시지만 편집 가능
     * - 발송 후 5분 이내에만 편집 허용
     * - 편집 이력은 별도 추적
     */
    private Boolean isEdited;

    /**
     * 메시지 삭제 여부
     * 
     * true: 삭제된 메시지 (UI에서 "삭제된 메시지" 표시)
     * false: 정상 메시지
     * 
     * 삭제 정책:
     * - 본인이 보낸 메시지만 삭제 가능
     * - 관리자는 모든 메시지 삭제 가능
     * - 삭제 후에도 DB에서는 유지 (감사 목적)
     */
    private Boolean isDeleted;

    /**
     * 메시지 생성 시 기본값 설정 (기존 Builder 확장)
     * 
     * @param roomCode 채팅방 코드 (기존 chatRoomId 대신)
     * @param senderType 발송자 타입
     * @param senderId 발송자 ID  
     * @param senderName 발송자 이름
     * @param content 메시지 내용
     * @param isSystemMessage 시스템 메시지 여부
     * @param messageType 메시지 타입 (추가)
     * @param fileInfoJson 파일 정보 (추가)
     */
    @Builder
    public ChatMessage(String roomCode, String senderType, Long senderId, String senderName,
                      String content, Boolean isSystemMessage, String messageType, String fileInfoJson) {
        this.roomCode = roomCode;
        this.senderType = senderType;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.isSystemMessage = isSystemMessage != null ? isSystemMessage : false;
        this.messageType = messageType != null ? messageType : "TEXT";
        this.fileInfoJson = fileInfoJson;
        this.isEdited = false;
        this.isDeleted = false;
        this.readStatusJson = "{}";  // 빈 JSON 객체로 초기화
        this.sentAt = LocalDateTime.now();
    }

    /**
     * 메시지 내용 수정
     * 
     * @param newContent 새로운 메시지 내용
     */
    public void editMessage(String newContent) {
        this.content = newContent;
        this.isEdited = true;
    }

    /**
     * 메시지 삭제 (논리적 삭제)
     */
    public void deleteMessage() {
        this.isDeleted = true;
        this.content = "삭제된 메시지입니다.";
    }

    /**
     * 시스템 메시지 생성을 위한 정적 팩토리 메서드
     * 
     * @param roomCode 채팅방 코드
     * @param messageType 시스템 메시지 타입
     * @param content 시스템 메시지 내용
     * @return 시스템 메시지 객체
     */
    public static ChatMessage createSystemMessage(String roomCode, String messageType, String content) {
        return ChatMessage.builder()
                .roomCode(roomCode)
                .senderId(0L)  // 시스템 메시지는 senderId = 0
                .senderName("시스템")
                .senderType("SYSTEM")
                .messageType(messageType)
                .content(content)
                .isSystemMessage(true)
                .build();
    }

    /**
     * 입장 알림 시스템 메시지 생성
     * 
     * @param roomCode 채팅방 코드
     * @param memberName 입장한 사용자 이름
     * @return 입장 알림 메시지
     */
    public static ChatMessage createEnterMessage(String roomCode, String memberName) {
        return createSystemMessage(roomCode, "SYSTEM_ENTER", 
                memberName + "님이 채팅방에 입장하셨습니다.");
    }

    /**
     * 퇴장 알림 시스템 메시지 생성
     * 
     * @param roomCode 채팅방 코드
     * @param memberName 퇴장한 사용자 이름
     * @return 퇴장 알림 메시지
     */
    public static ChatMessage createLeaveMessage(String roomCode, String memberName) {
        return createSystemMessage(roomCode, "SYSTEM_LEAVE",
                memberName + "님이 채팅방을 나가셨습니다.");
    }
}
