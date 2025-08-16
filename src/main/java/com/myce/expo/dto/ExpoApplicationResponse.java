package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 플랫폼 관리자용 박람회 신청 목록 조회 응답 DTO
 */
@Builder
@Getter
public class ExpoApplicationResponse {
    
    /**
     * 박람회 ID
     */
    private Long id;
    
    /**
     * 신청자 아이디
     */
    private String memberUsername;
    
    /**
     * 신청자 이름
     */
    private String memberName;
    
    /**
     * 박람회 제목
     */
    private String title;
    
    /**
     * 박람회 위치
     */
    private String location;
    
    /**
     * 신청자 이메일
     */
    private String memberEmail;
    
    /**
     * 신청자 전화번호
     */
    private String memberPhone;
    
    /**
     * 신청일시
     */
    private LocalDateTime createdAt;
    
    /**
     * 박람회 상태 메시지
     */
    private String statusMessage;
    
    /**
     * 박람회 상태 (enum)
     */
    private String status;
    
    /**
     * 게시 시작일 (자동 게시 예정일 표시용)
     */
    private LocalDate displayStartDate;
}