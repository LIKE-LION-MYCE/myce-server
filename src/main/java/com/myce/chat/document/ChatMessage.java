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

    private String messageType;

    private String fileInfoJson;

    private String readStatusJson;

    private Boolean isEdited;

    private Boolean isDeleted;

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
     */
    public void editMessage(String newContent) {
        this.content = newContent;
        this.isEdited = true;
    }

    /**
     * 메시지 삭제
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
                .senderType("SYSTEM")
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
