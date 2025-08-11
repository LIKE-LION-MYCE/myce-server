package com.myce.member.mapper.ad;

import com.myce.advertisement.entity.Advertisement;
import com.myce.common.entity.BusinessProfile;
import com.myce.member.dto.ad.AdvertisementPaymentDetailResponse;
import com.myce.payment.entity.AdPaymentInfo;
import org.springframework.stereotype.Component;

@Component
public class AdvertisementPaymentDetailMapper {
    
    public AdvertisementPaymentDetailResponse toAdvertisementPaymentDetailResponse(Advertisement advertisement,
                                                                                 BusinessProfile businessProfile,
                                                                                 AdPaymentInfo adPaymentInfo) {
        return AdvertisementPaymentDetailResponse.builder()
                .advertisementTitle(advertisement.getTitle())
                .applicantName(businessProfile.getCompanyName())
                .displayStartDate(advertisement.getDisplayStartDate())
                .displayEndDate(advertisement.getDisplayEndDate())
                .totalDays(adPaymentInfo.getTotalDay())
                .feePerDay(adPaymentInfo.getFeePerDay())
                .totalAmount(adPaymentInfo.getTotalAmount())
                .status(advertisement.getStatus())
                .build();
    }
}