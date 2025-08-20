package com.myce.chat.service.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 읽음 상태 처리 유틸리티 클래스
 * 
 * 기존 ChatRoomServiceImpl과 ExpoChatServiceImpl에서 중복으로 구현된
 * 읽음 상태 JSON 업데이트 로직을 통합하여 일관성과 유지보수성 향상
 */
@Slf4j
public final class ChatReadStatusUtil {

    private ChatReadStatusUtil() {
        // 유틸리티 클래스는 인스턴스화 방지
    }

    /**
     * 사용자(USER) 읽음 상태 업데이트
     * 
     * @param currentReadStatus 현재 읽음 상태 JSON
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @return 업데이트된 읽음 상태 JSON
     */
    public static String updateReadStatusForUser(String currentReadStatus, String lastReadMessageId) {
        if (currentReadStatus == null || currentReadStatus.isEmpty() || currentReadStatus.equals("{}")) {
            return "{\"USER\":\"" + lastReadMessageId + "\"}";
        }
        
        // 기존 USER 정보가 있으면 업데이트, 없으면 추가
        if (currentReadStatus.contains("\"USER\"")) {
            return currentReadStatus.replaceAll("\"USER\":\"[^\"]*\"", "\"USER\":\"" + lastReadMessageId + "\"");
        } else {
            return currentReadStatus.substring(0, currentReadStatus.length() - 1) + 
                   ",\"USER\":\"" + lastReadMessageId + "\"}";
        }
    }

    /**
     * 관리자(ADMIN) 읽음 상태 업데이트
     * 
     * @param currentReadStatus 현재 읽음 상태 JSON
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @return 업데이트된 읽음 상태 JSON
     */
    public static String updateReadStatusForAdmin(String currentReadStatus, String lastReadMessageId) {
        if (currentReadStatus == null || currentReadStatus.isEmpty() || currentReadStatus.equals("{}")) {
            return "{\"ADMIN\":\"" + lastReadMessageId + "\"}";
        }
        
        // 기존 ADMIN 정보가 있으면 업데이트, 없으면 추가
        if (currentReadStatus.contains("\"ADMIN\"")) {
            return currentReadStatus.replaceAll("\"ADMIN\":\"[^\"]*\"", "\"ADMIN\":\"" + lastReadMessageId + "\"");
        } else {
            return currentReadStatus.substring(0, currentReadStatus.length() - 1) + 
                   ",\"ADMIN\":\"" + lastReadMessageId + "\"}";
        }
    }

    /**
     * 읽음 상태 JSON에서 특정 사용자 타입의 마지막 읽은 메시지 ID 추출
     * 
     * @param readStatusJson 읽음 상태 JSON
     * @param userType 사용자 타입 ("USER" 또는 "ADMIN")
     * @return 마지막으로 읽은 메시지 ID, 없으면 null
     */
    public static String extractLastReadMessageId(String readStatusJson, String userType) {
        try {
            if (readStatusJson == null || readStatusJson.isEmpty() || readStatusJson.equals("{}")) {
                return null;
            }
            
            // 간단한 JSON 파싱 (Jackson 라이브러리 사용하지 않고)
            String searchKey = "\"" + userType + "\":\"";
            int startIndex = readStatusJson.indexOf(searchKey);
            if (startIndex == -1) {
                return null;
            }
            
            startIndex += searchKey.length();
            int endIndex = readStatusJson.indexOf("\"", startIndex);
            if (endIndex == -1) {
                return null;
            }
            
            return readStatusJson.substring(startIndex, endIndex);
            
        } catch (Exception e) {
            log.warn("readStatusJson 파싱 실패: {}", readStatusJson, e);
            return null;
        }
    }

    /**
     * 읽음 상태 JSON 유효성 검증
     * 
     * @param readStatusJson 검증할 JSON 문자열
     * @return 유효한 JSON이면 true
     */
    public static boolean isValidReadStatusJson(String readStatusJson) {
        if (readStatusJson == null || readStatusJson.isEmpty()) {
            return true; // 빈 값은 유효함 (초기 상태)
        }
        
        try {
            // 기본적인 JSON 형식 확인
            return readStatusJson.startsWith("{") && 
                   readStatusJson.endsWith("}") && 
                   (readStatusJson.contains("\"USER\"") || readStatusJson.contains("\"ADMIN\"") || 
                    readStatusJson.equals("{}"));
        } catch (Exception e) {
            log.warn("읽음 상태 JSON 유효성 검증 실패: {}", readStatusJson, e);
            return false;
        }
    }

    /**
     * 빈 읽음 상태 JSON 생성
     * 
     * @return 초기 상태의 빈 JSON 문자열
     */
    public static String createEmptyReadStatus() {
        return "{}";
    }

    /**
     * 전체 읽음 상태 JSON 생성 (USER, ADMIN 모두 동일한 메시지 ID로 설정)
     * 
     * @param messageId 설정할 메시지 ID
     * @return 전체 읽음 처리된 JSON 문자열
     */
    public static String createFullReadStatus(String messageId) {
        return String.format("{\"USER\":\"%s\",\"ADMIN\":\"%s\"}", messageId, messageId);
    }

    /**
     * 읽음 상태 JSON에서 특정 사용자 타입이 읽지 않은 메시지인지 확인
     * 
     * @param readStatusJson 읽음 상태 JSON
     * @param userType 확인할 사용자 타입 ("USER" 또는 "ADMIN")
     * @param messageId 확인할 메시지 ID
     * @return 읽지 않은 메시지면 true
     */
    public static boolean isUnreadMessage(String readStatusJson, String userType, String messageId) {
        String lastReadId = extractLastReadMessageId(readStatusJson, userType);
        
        if (lastReadId == null || lastReadId.isEmpty()) {
            return true; // 아무것도 읽지 않았으면 읽지 않은 메시지
        }
        
        // MongoDB ObjectId는 시간순 정렬이 가능하므로 문자열 비교로 판단
        return messageId.compareTo(lastReadId) > 0;
    }
}