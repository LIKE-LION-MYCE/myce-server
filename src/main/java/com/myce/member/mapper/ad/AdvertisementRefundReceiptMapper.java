package com.myce.member.mapper.ad;

import com.myce.advertisement.entity.Advertisement;
import com.myce.common.entity.BusinessProfile;
import com.myce.member.dto.ad.AdvertisementRefundReceiptResponse;
import com.myce.payment.entity.AdPaymentInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class AdvertisementRefundReceiptMapper {
    
    public AdvertisementRefundReceiptResponse toRefundReceiptDto(Advertisement advertisement, 
                                                                BusinessProfile businessProfile, 
                                                                AdPaymentInfo adPaymentInfo) {
        
        // 환불 계산
        LocalDate today = LocalDate.now();
        LocalDate startDate = advertisement.getDisplayStartDate();
        
        // 사용한 일수 계산 (시작일부터 오늘까지)
        int usedDays = (int) ChronoUnit.DAYS.between(startDate, today) + 1; // +1은 시작일 포함
        if (usedDays < 0) usedDays = 0;
        
        // 남은 일수 계산
        int remainingDays = adPaymentInfo.getTotalDay() - usedDays;
        if (remainingDays < 0) remainingDays = 0;
        
        // 금액 계산
        int usedAmount = usedDays * adPaymentInfo.getFeePerDay();
        int refundAmount = remainingDays * adPaymentInfo.getFeePerDay();
        
        return AdvertisementRefundReceiptResponse.builder()
                .advertisementTitle(advertisement.getTitle())
                .applicantName(businessProfile.getCompanyName())
                .displayStartDate(advertisement.getDisplayStartDate())
                .displayEndDate(advertisement.getDisplayEndDate())
                .status(advertisement.getStatus())
                .totalDays(adPaymentInfo.getTotalDay())
                .feePerDay(adPaymentInfo.getFeePerDay())
                .totalAmount(adPaymentInfo.getTotalAmount())
                .refundRequestDate(today)
                .usedDays(usedDays)
                .usedAmount(usedAmount)
                .remainingDays(remainingDays)
                .refundAmount(refundAmount)
                .build();
    }
}