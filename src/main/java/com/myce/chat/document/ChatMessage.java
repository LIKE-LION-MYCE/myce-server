package com.myce.chat.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.myce.chat.type.MessageSenderType;

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
     */
    public static ChatMessage createSystemMessage(String roomCode, String messageType, String content) {
        return ChatMessage.builder()
                .roomCode(roomCode)
                .senderId(0L)  // 시스템 메시지는 senderId = 0
                .senderName("시스템")
                .senderType(MessageSenderType.SYSTEM.name())
                .messageType(messageType)
                .content(content)
                .isSystemMessage(true)
                .build();
    }

    /**
     * 입장 알림 시스템 메시지 생성
     */
    public static ChatMessage createEnterMessage(String roomCode, String memberName) {
        return createSystemMessage(roomCode, "SYSTEM_ENTER", 
                memberName + "님이 채팅방에 입장하셨습니다.");
    }

    /**
     * 퇴장 알림 시스템 메시지 생성
     */
    public static ChatMessage createLeaveMessage(String roomCode, String memberName) {
        return createSystemMessage(roomCode, "SYSTEM_LEAVE",
                memberName + "님이 채팅방을 나가셨습니다.");
    }
}
