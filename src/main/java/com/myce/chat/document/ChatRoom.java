package com.myce.chat.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

@Getter @NoArgsConstructor
@Document(collection = "chat_rooms")
public class ChatRoom {

    @Id
    private String id;
    private String roomCode;
    private Long memberId;
    private String memberName;
    private Long expoId;
    private String expoName;
    private Boolean isActive;
    private String lastMessageId;
    private LocalDateTime lastMessageAt;
    private String lastReadMessageId;
    private LocalDateTime createdAt;

    @Builder
    public ChatRoom(String roomCode, Long memberId, Long expoId, String memberName, String expoName, Boolean isActive) {
        this.roomCode = roomCode;
        this.memberId = memberId;
        this.memberName = memberName;
        this.expoId = expoId;
        this.expoName = expoName;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
    }

    public void updateLastMessageInfo(String lastMessageId) {
        this.lastMessageId = lastMessageId;
        this.lastMessageAt = LocalDateTime.now();
    }
}
