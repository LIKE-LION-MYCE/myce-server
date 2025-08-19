package com.myce.chat.service;

import com.myce.member.entity.type.Role;

/**
 * 채팅방 접근 권한 캐시 서비스 인터페이스
 */
public interface ChatRoomAccessCacheService {

    /**
     * 채팅방 접근 권한 정보 조회
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     * @param userRole 사용자 역할
     * @return 접근 권한 정보 (캐시 미스 시 null)
     */
    ChatRoomAccessInfo getCachedAccessInfo(String roomCode, Long userId, Role userRole);

    /**
     * 채팅방 접근 권한 정보 캐싱
     * @param roomCode 채팅방 코드
     * @param userId 사용자 ID
     * @param userRole 사용자 역할
     * @param accessInfo 접근 권한 정보
     */
    void cacheAccessInfo(String roomCode, Long userId, Role userRole, ChatRoomAccessInfo accessInfo);

    /**
     * 특정 사용자의 모든 채팅방 접근 권한 캐시 무효화
     * @param userId 사용자 ID
     */
    void invalidateUserAccessCache(Long userId);

    /**
     * 특정 채팅방의 모든 접근 권한 캐시 무효화
     * @param roomCode 채팅방 코드
     */
    void invalidateRoomAccessCache(String roomCode);

    /**
     * 채팅방 접근 권한 정보 DTO (Jackson 직렬화/역직렬화 호환)
     */
    class ChatRoomAccessInfo {
        private boolean hasAccess;
        private String accessLevel; // USER, EXPO_ADMIN, PLATFORM_ADMIN
        private Long expoId;
        private String adminCode;
        private long cacheTimestamp;

        // Jackson 역직렬화를 위한 기본 생성자
        public ChatRoomAccessInfo() {
            this.cacheTimestamp = System.currentTimeMillis();
        }

        // 기존 생성자 유지
        public ChatRoomAccessInfo(boolean hasAccess, String accessLevel, Long expoId, String adminCode) {
            this.hasAccess = hasAccess;
            this.accessLevel = accessLevel;
            this.expoId = expoId;
            this.adminCode = adminCode;
            this.cacheTimestamp = System.currentTimeMillis();
        }

        // Getters (Jackson 직렬화용)
        public boolean isHasAccess() { return hasAccess; }  // Jackson 표준: isXxx() for boolean
        public boolean hasAccess() { return hasAccess; }    // 기존 호환성 유지
        public String getAccessLevel() { return accessLevel; }
        public Long getExpoId() { return expoId; }
        public String getAdminCode() { return adminCode; }
        public long getCacheTimestamp() { return cacheTimestamp; }

        // Jackson 역직렬화를 위한 Setters
        public void setHasAccess(boolean hasAccess) { this.hasAccess = hasAccess; }
        public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
        public void setExpoId(Long expoId) { this.expoId = expoId; }
        public void setAdminCode(String adminCode) { this.adminCode = adminCode; }
        public void setCacheTimestamp(long cacheTimestamp) { this.cacheTimestamp = cacheTimestamp; }

        // 디버깅을 위한 toString 메서드
        @Override
        public String toString() {
            return String.format("ChatRoomAccessInfo{hasAccess=%s, accessLevel='%s', expoId=%s, adminCode='%s', cacheTimestamp=%d}", 
                               hasAccess, accessLevel, expoId, adminCode, cacheTimestamp);
        }

        // Builder pattern for team consistency
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean hasAccess;
            private String accessLevel;
            private Long expoId;
            private String adminCode;

            public Builder hasAccess(boolean hasAccess) {
                this.hasAccess = hasAccess;
                return this;
            }

            public Builder accessLevel(String accessLevel) {
                this.accessLevel = accessLevel;
                return this;
            }

            public Builder expoId(Long expoId) {
                this.expoId = expoId;
                return this;
            }

            public Builder adminCode(String adminCode) {
                this.adminCode = adminCode;
                return this;
            }

            public ChatRoomAccessInfo build() {
                return new ChatRoomAccessInfo(hasAccess, accessLevel, expoId, adminCode);
            }
        }
    }
}