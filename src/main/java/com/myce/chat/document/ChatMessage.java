package com.myce.chat.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Getter
@NoArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndexes({
    @CompoundIndex(name = "room_time_idx", def = "{'roomCode': 1, 'sentAt': -1}"),
    @CompoundIndex(name = "sender_time_idx", def = "{'senderId': 1, 'sentAt': -1}")
})
public class ChatMessage {
    @Id
    private String id;

    @Indexed
    private String roomCode;
    private String senderType;

    @Indexed
    private Long senderId;

    private String senderName;

    private String content;

    @CreatedDate
    @Indexed
    private LocalDateTime sentAt;

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
