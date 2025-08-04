package com.myce.chat.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter @NoArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;
    private String chatRoomId;
    private String senderType;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isSystemMessage;


    @Builder
    public ChatMessage(String chatRoomId, String senderType, Long senderId, String senderName,
                       String content, Boolean isSystemMessage) {
        this.chatRoomId = chatRoomId;
        this.senderType = senderType;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.isSystemMessage = isSystemMessage;
        this.sentAt = LocalDateTime.now();
    }


}
