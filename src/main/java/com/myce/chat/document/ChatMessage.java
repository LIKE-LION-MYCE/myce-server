package com.myce.chat.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    private String id; // ObjectId 문자열로 매핑

    private String chatRoomId;

    private String senderType; // 예: ADMIN, USER 등

    private String senderId;

    private String content;

    private LocalDateTime sentAt;

    private Boolean isSystemMessage;
}
