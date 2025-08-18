package com.myce.reservation.dto;

import com.myce.member.entity.type.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDetailResponse {
    
    private ExpoInfo expoInfo;
    private ReservationInfo reservationInfo;
    private List<ReserverInfo> reserverInfos;
    private PaymentInfo paymentInfo;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExpoInfo {
        private Long expoId;
        private String thumbnailUrl;
        private String title;
        private String location;
        private String locationDetail;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate displayStartDate;
        private LocalDate displayEndDate;
        private LocalTime startTime;
        private LocalTime endTime;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationInfo {
        private Long reservationId;
        private String reservationCode;
        private String status;
        private Integer quantity;
        private LocalDateTime createdAt;
        private Integer ticketPrice;
        private String ticketName;
        private String ticketType;
        private LocalDate ticketUseStartDate;
        private LocalDate ticketUseEndDate;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReserverInfo {
        private Long reserverId;
        private String name;
        private Gender gender;
        private String phone;
        private String email;
        private String qrCodeUrl;
        private String qrStatus;
        private LocalDateTime qrUsedAt;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private Integer usedMileage;
        private Integer savedMileage;
        private Integer totalAmount;
        private String paymentStatus;
        private String paymentMethod;
        private String paymentDetail;
        private LocalDateTime paidAt;
        private String memberGrade;
        private BigDecimal mileageRate;
        private String gradeDescription;
    }
}