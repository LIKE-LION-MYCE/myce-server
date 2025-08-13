package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 플랫폼 관리자용 박람회 신청 상세 조회 응답 DTO
 */
@Builder
@Getter
public class ExpoApplicationDetailResponse {
    
    /**
     * 박람회 ID
     */
    private Long id;
    
    /**
     * 박람회 제목
     */
    private String title;
    
    /**
     * 박람회 설명
     */
    private String description;
    
    /**
     * 박람회 위치
     */
    private String location;
    
    /**
     * 박람회 상세 위치
     */
    private String locationDetail;
    
    /**
     * 박람회 시작일
     */
    private LocalDate startDate;
    
    /**
     * 박람회 종료일
     */
    private LocalDate endDate;
    
    /**
     * 게시 시작일
     */
    private LocalDate displayStartDate;
    
    /**
     * 게시 종료일
     */
    private LocalDate displayEndDate;
    
    /**
     * 박람회 시작 시간
     */
    private LocalTime startTime;
    
    /**
     * 박람회 종료 시간
     */
    private LocalTime endTime;
    
    /**
     * 최대 예약자 수
     */
    private Integer maxReserverCount;
    
    /**
     * 프리미엄 여부
     */
    private Boolean isPremium;
    
    /**
     * 박람회 상태
     */
    private String status;
    
    /**
     * 박람회 상태 라벨
     */
    private String statusLabel;
    
    /**
     * 썸네일 URL
     */
    private String thumbnailUrl;
    
    /**
     * 신청일시
     */
    private LocalDateTime createdAt;
    
    /**
     * 신청자 정보
     */
    private ApplicantInfo applicant;
    
    /**
     * 사업자 정보
     */
    private BusinessInfo business;
    
    /**
     * 신청자 정보 내부 클래스
     */
    @Builder
    @Getter
    public static class ApplicantInfo {
        
        /**
         * 회원 ID
         */
        private Long memberId;
        
        /**
         * 회원 아이디
         */
        private String loginId;
        
        /**
         * 회원 이름
         */
        private String name;
        
        /**
         * 회원 이메일
         */
        private String email;
        
        /**
         * 회원 전화번호
         */
        private String phone;
        
        /**
         * 회원 성별
         */
        private String gender;
        
        /**
         * 회원 생년월일
         */
        private LocalDate birth;
    }
    
    /**
     * 사업자 정보 내부 클래스
     */
    @Builder
    @Getter
    public static class BusinessInfo {
        
        /**
         * 회사명
         */
        private String companyName;
        
        /**
         * 대표자명
         */
        private String ceoName;
        
        /**
         * 회사 주소
         */
        private String address;
        
        /**
         * 연락처
         */
        private String contactPhone;
        
        /**
         * 이메일
         */
        private String contactEmail;
        
        /**
         * 사업자등록번호
         */
        private String businessRegistrationNumber;
    }
}