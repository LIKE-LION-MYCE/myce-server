package com.myce.expo.dto;

import com.myce.expo.entity.type.ExpoStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class ExpoDetailResponse {
    // 기본 박람회 정보
    private Long expoId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private ExpoStatus status;
    
    // 장소 정보
    private String location;
    private String locationDetail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    // 일정 정보
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    
    // 예약 정보
    private Integer maxReserverCount;
    private Integer currentReservationCount; // 현재 예약자 수
    private Integer remainingTickets; // 남은 티켓 수
    
    // 티켓 정보
    private List<TicketInfo> tickets;
    
    // 찜하기 정보
    private Boolean isBookmarked; // 현재 사용자가 찜했는지 여부
    private Integer bookmarkCount; // 총 찜 수
    
    // 리뷰 정보
    private Double averageRating; // 평균 평점
    private Integer reviewCount; // 리뷰 수
    private List<ReviewInfo> recentReviews; // 최근 리뷰 3개
    
    // 주최자 정보
    private String organizerName;
    private String organizerContact;
    private OrganizerInfo organizerInfo;
    
    // 카테고리
    private List<String> categories;
    
    @Getter
    @Builder
    public static class TicketInfo {
        private Long ticketId;
        private String name;
        private String type; // EARLY_BIRD, REGULAR
        private Integer price;
        private Integer totalQuantity;
        private Integer remainingQuantity;
        private String description;
    }
    
    @Getter
    @Builder
    public static class ReviewInfo {
        private Long reviewId;
        private String memberName;
        private Integer rating;
        private String comment;
        private LocalDate createdAt;
    }
    
    @Getter
    @Builder
    public static class OrganizerInfo {
        private String companyName;
        private String ceoName;
        private String contactPhone;
        private String contactEmail;
        private String address;
        private String businessRegistrationNumber;
    }
}