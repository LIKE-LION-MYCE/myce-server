package com.myce.chat.document;

import lombok.*;
import org.bson.types.ObjectId;
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
     */
    private String messageType;

    /**
     * 파일 정보 (JSON 형태) - messageType이 IMAGE 또는 FILE인 경우 사용
     */
    private String fileInfoJson;

    /**
     * 메시지 읽음 상태 (JSON 형태)
     */
    private String readStatusJson;

    /**
     * 메시지 편집 여부
     */
    private Boolean isEdited;

    /**
     * 메시지 삭제 여부
     */
    private Boolean isDeleted;

    /**
     * 실제 발송자 (관리자용)
     * AdminCode: "CODE123A", Super Admin: "SUPER_ADMIN" 
     */
    private String actualSender;

    /**
     * 로그인 타입 ("MEMBER" | "ADMIN_CODE")
     */
    private String loginType;

    /**
     * 메시지 생성 시 기본값 설정
     * MongoDB ObjectId를 미리 생성하여 ID 일관성 보장
     */
    @Builder
    public ChatMessage(String roomCode, String senderType, Long senderId, String senderName,
                      String content, Boolean isSystemMessage, String messageType, String fileInfoJson) {
        // MongoDB ObjectId를 Java에서 미리 생성 (MongoDB 접속 없이 로컬 생성)
        this.id = new ObjectId().toString();
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
        this.readStatusJson = "{}";
        this.sentAt = LocalDateTime.now();
    }

    /**
     * 메시지 내용 수정
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
     * 관리자 메시지 생성 (기존 패턴 유지)
     */
    public static ChatMessage createAdminMessage(String roomCode, String content, 
                                                Long adminId, String adminCode, 
                                                String loginType, String displayName) {
        return ChatMessage.builder()
            .roomCode(roomCode)
            .senderType("ADMIN")
            .senderId(adminId)
            .senderName(displayName)
            .content(content)
            .isSystemMessage(false)
            .messageType("TEXT")
            .fileInfoJson(null)
            .build()
            .setAdminInfo(adminCode, loginType);
    }

    /**
     * Admin 정보 설정
     */
    private ChatMessage setAdminInfo(String adminCode, String loginType) {
        this.actualSender = adminCode;
        this.loginType = loginType;
        return this;
    }

    /**
     * 시스템 메시지 생성 (상태 전환용)
     */
    public static ChatMessage createSystemMessage(String roomCode, String content) {
        return ChatMessage.builder()
            .roomCode(roomCode)
            .senderType("SYSTEM")
            .senderId(-99L)
            .senderName("시스템")
            .content(content)
            .isSystemMessage(true)
            .messageType("SYSTEM")
            .fileInfoJson(null)
            .build();
    }

}
