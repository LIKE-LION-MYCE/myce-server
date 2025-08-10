package com.myce.advertisement.service.mapper;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.RejectInfo;
import com.myce.member.entity.Member;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentMethod;

public class AdvertisementMapper {
    public static SimpleApplyAdvertisement getSimpleAdvertisement(Advertisement advertisement,
              BusinessProfile businessProfile) {
        Member member = advertisement.getMember();
        AdPosition adPosition = advertisement.getAdPosition();

        return SimpleApplyAdvertisement.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .memberUsername(member.getLoginId())
                .memberNickname(member.getName())
                .memberEmail(member.getEmail())
                .bannerLocationName(adPosition.getName())
                .createdAt(advertisement.getCreatedAt())
                .memberPhone(businessProfile.getContactPhone())
                .statusMessage(advertisement.getStatus().name())
                .build();
    }

    public static DetailApplyAdvertisement getDetailAdvertisement(Advertisement advertisement,
              BusinessProfile businessProfile) {
        AdPosition adPosition = advertisement.getAdPosition();

        return DetailApplyAdvertisement.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .statusMessage(advertisement.getStatus().name())
                .bannerLocationName(adPosition.getName())
                .bannerImageUrl(advertisement.getImageUrl())
                .startAt(advertisement.getDisplayStartDate())
                .endAt(advertisement.getDisplayEndDate())
                .description(advertisement.getDescription())
                .businessCompany(businessProfile.getCompanyName())
                .representName(businessProfile.getCeoName())
                .businessEmail(businessProfile.getContactEmail())
                .businessPhone(businessProfile.getContactPhone())
                .address(businessProfile.getAddress())
                .businessNumber(businessProfile.getBusinessRegistrationNumber())
                .build();
    }

    public static AdRejectInfoResponse getAdRejectInfoResponse(RejectInfo rejectInfo) {
        return AdRejectInfoResponse.builder()
                .description(rejectInfo.getDescription())
                .build();
    }

    public static AdPaymentInfoResponse getPaymentInfoRequest(AdPaymentInfo adPaymentInfo){
        Advertisement advertisement = adPaymentInfo.getAdvertisement();

        return AdPaymentInfoResponse.builder()
                .title(advertisement.getTitle())
                .requesterName(advertisement.getMember().getName())
                .startAt(advertisement.getDisplayStartDate())
                .endAt(advertisement.getDisplayEndDate())
                .totalPrice(adPaymentInfo.getFeePerDay() * adPaymentInfo.getTotalDay())
                .totalPayment(adPaymentInfo.getTotalAmount())
                .build();
    }

    public static AdCancelInfoResponse getAdCancelInfoResponse(Advertisement advertisement, Payment payment, Refund refund) {
        PaymentMethod paymentMethod = payment.getPaymentMethod();
        String paymentCompanyName;
        String paymentAccountInfo;
        // 계좌이체일때 - account_number
        // 나머지 - card_number
        // todo: EASY_PAY, FOREIGN_PAY 어떻게 처리할지
        if(paymentMethod == PaymentMethod.TRANSFER){
            paymentCompanyName = payment.getAccountBank();
            paymentAccountInfo = payment.getAccountNumber();
        }else{
            paymentCompanyName = payment.getCardCompany();
            paymentAccountInfo = payment.getCardNumber();
        }

        return AdCancelInfoResponse.builder()
                .title(advertisement.getTitle())
                .requesterName(advertisement.getMember().getName())
                .startAt(advertisement.getDisplayStartDate())
                .endAt(advertisement.getDisplayEndDate())
                .paymentType(paymentMethod.name())
                .paymentCompanyName(paymentCompanyName)
                .paymentAccountInfo(paymentAccountInfo)
                .totalAmount(refund.getAmount())
                .build();
    }

}
