package com.myce.advertisement.service.mapper;

import com.myce.advertisement.dto.*;
import com.myce.advertisement.entity.Advertisement;
import com.myce.common.entity.RejectInfo;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentMethod;

import java.util.HashMap;

public class AdInfoMapper {
    public static AdCancelInfoCheck getAdCancelInfoCheck(
            Payment payment, Advertisement ad, Integer totalAmount) {
        PaymentTypeResult paymentTypeResult = getResult(payment, payment.getPaymentMethod());

        return AdCancelInfoCheck.builder()
                .title(ad.getTitle())
                .requesterName(ad.getMember().getName())
                .startAt(ad.getDisplayStartDate())
                .endAt(ad.getDisplayEndDate())
                .paymentType(payment.getPaymentMethod().name())
                .paymentCompanyName(paymentTypeResult.paymentCompanyName)
                .paymentAccountInfo(paymentTypeResult.paymentAccountInfo)
                .totalAmount(totalAmount)
                .build();
    }

    public static AdPaymentHistoryResponse getPaymentInfoResponse(AdPaymentInfo adPaymentInfo,
                                                                  Payment payment){
        Advertisement advertisement = adPaymentInfo.getAdvertisement();
        PaymentTypeResult paymentTypeResult = getResult(payment, payment.getPaymentMethod());

        return AdPaymentHistoryResponse.builder()
                .title(advertisement.getTitle())
                .requesterName(advertisement.getMember().getName())
                .startAt(advertisement.getDisplayStartDate())
                .endAt(advertisement.getDisplayEndDate())
                .paymentType(payment.getPaymentMethod().name())
                .paymentCompanyName(paymentTypeResult.paymentCompanyName)
                .paymentAccountInfo(paymentTypeResult.paymentAccountInfo)
                .totalPrice(adPaymentInfo.getFeePerDay() * adPaymentInfo.getTotalDay())
                .totalPayment(adPaymentInfo.getTotalAmount())
                .build();
    }

    public static AdCancelHistoryResponse getAdCancelInfoResponse(
            Advertisement advertisement, Payment payment, Refund refund) {
        PaymentMethod paymentMethod = payment.getPaymentMethod();
        PaymentTypeResult paymentTypeResult = getResult(payment, paymentMethod);

        return AdCancelHistoryResponse.builder()
                .title(advertisement.getTitle())
                .requesterName(advertisement.getMember().getName())
                .startAt(advertisement.getDisplayStartDate())
                .endAt(advertisement.getDisplayEndDate())
                .paymentType(paymentMethod.name())
                .paymentCompanyName(paymentTypeResult.paymentCompanyName())
                .paymentAccountInfo(paymentTypeResult.paymentAccountInfo())
                .totalAmount(refund.getAmount())
                .build();
    }

    public static AdRejectInfoResponse getAdRejectInfoResponse(RejectInfo rejectInfo) {
        return AdRejectInfoResponse.builder()
                .description(rejectInfo.getDescription())
                .build();
    }

    public static AdPaymentInfoCheck getAdPaymentForm(
            Advertisement ad, HashMap<String, Integer> priceMap, int totalDays, int totalPayment) {
        return AdPaymentInfoCheck.builder()
                .title(ad.getTitle())
                .requesterName(ad.getMember().getName())
                .startAt(ad.getDisplayStartDate())
                .endAt(ad.getDisplayEndDate())
                .totalDays(totalDays)
                .priceMap(priceMap)
                .totalPayment(totalPayment)
                .build();
    }

    private static PaymentTypeResult getResult(Payment payment, PaymentMethod paymentMethod) {
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
        return new PaymentTypeResult(paymentCompanyName, paymentAccountInfo);
    }

    private record PaymentTypeResult(String paymentCompanyName, String paymentAccountInfo) {
    }
}
