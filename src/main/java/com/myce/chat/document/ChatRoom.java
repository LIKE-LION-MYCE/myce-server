package com.myce.chat.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

@Document(collection = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    private String chatRoomId;

    private String roomCode;

    private Long memberId;
    private Long expoId;

    private Boolean isActive;

    private String lastMessageId;
    private LocalDateTime lastMessageAt;

    private String lastReadMessageId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
