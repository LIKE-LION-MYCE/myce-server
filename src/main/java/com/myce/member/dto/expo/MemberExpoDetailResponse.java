package com.myce.member.dto.expo;

import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.entity.type.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberExpoDetailResponse {
    
    // 박람회 기본 정보
    private Long expoId;
    private String title;
    private String thumbnailUrl;
    private String location;
    private String locationDetail;
    private Integer maxReserverCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private String description;
    private ExpoStatus status;
    private Boolean isPremium;           // 프리미엄 여부
    private String category;             // 카테고리명
    
    // 회원 정보
    private String memberLoginId;        // 회원 로그인 ID
    
    // 결제 정보
    private PaymentInfo paymentInfo;
    
    // 티켓 정보 리스트
    private List<TicketInfo> tickets;
    
    // 사업자 정보
    private BusinessInfo businessInfo;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private Integer deposit;              // 등록금
        private Integer premiumDeposit;       // 프리미엄 등록금
        private Integer totalAmount;          // 총 결제 금액 (일일사용료 * 게시기간 + 등록금)
        private Integer dailyUsageFee;        // 일일 사용료
        private Integer totalDay;             // 게시 기간(일)
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketInfo {
        private Long ticketId;
        private String name;                  // 티켓 이름
        private Integer price;                // 티켓 가격
        private Integer totalQuantity;        // 총 티켓 수
        private TicketType type;              // 티켓 타입
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusinessInfo {
        private String companyName;           // 회사명
        private String ceoName;              // 대표자명
        private String address;              // 회사주소
        private String contactPhone;         // 대표 연락처
        private String contactEmail;         // 대표 이메일
        private String businessRegistrationNumber; // 사업자번호
    }
}