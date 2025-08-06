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

    @Id
    private String id;

    /**
     * 채팅방 코드 (비즈니스 키)
     * 형식: "admin-{expoId}-{memberId}"
    */
    @Indexed(unique = true)
    private String roomCode;

    /**
     * 참가자 회원 ID
     */
    @Indexed
    private Long memberId;

    /**
     * 참가자 이름 (캐시용)
     */
    private String memberName;

    /**
     * 박람회 ID
     */
    @Indexed
    private Long expoId;

    /**
     * 박람회 제목 (캐시용)
     */
    private String expoTitle;

    /**
     * 채팅방 활성화 상태
     */
    @Indexed
    private Boolean isActive;

    /**
     * 마지막 메시지 내용 (미리보기용)
     */
    private String lastMessage;

    /**
     * 마지막 메시지 ID
     */
    private String lastMessageId;

    /**
     * 마지막 메시지 전송 시간
     */
    @Indexed
    private LocalDateTime lastMessageAt;

    /**
     * 각 사용자별 마지막 읽은 메시지 정보 (JSON 형태)
     */
    private String readStatusJson;

    /**
     * 채팅방 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 채팅방 정보 최종 수정 시간
     */
    @LastModifiedDate  
    private LocalDateTime updatedAt;

    /**
     * 채팅방 생성 시 기본값 설정
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
     */
    public void updateLastMessageInfo(String messageId, String messageContent) {
        this.lastMessageId = messageId;
        this.lastMessage = truncateMessage(messageContent);
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 채팅방 비활성화
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 채팅방 재활성화
     */
    public void reactivate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 메시지 내용 자르기 (미리보기용)
     */
    private String truncateMessage(String content) {
        if (content == null) return null;
        if (content.length() <= 200) return content;
        return content.substring(0, 197) + "...";
    }
}
