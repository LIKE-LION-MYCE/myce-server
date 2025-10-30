package com.myce.payment.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.payment.service.constant.PortOneResponseKey;
import com.myce.payment.service.constant.PortOneStatus;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VerifyPaymentService {

    public void verifyPaymentDetails(Map<String, Object> portOnePayment,
            int amount, String merchantUid) {
        String status = (String) portOnePayment.get(PortOneResponseKey.STATUS);
        Integer paidAmount = (Integer) portOnePayment.get(PortOneResponseKey.AMOUNT);
        String portOneMerchantUid = (String) portOnePayment.get(PortOneResponseKey.MERCHANT_UID);

        if (!PortOneStatus.PAID.equalsIgnoreCase(status)) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_PAID);
        }
        if (!paidAmount.equals(amount)) {
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (!portOneMerchantUid.equals(merchantUid)) {
            throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }
    }

    public void verifyVbankDetails(Map<String, Object> portOnePayment, int amount, String uid) {
        String status = (String) portOnePayment.get("status");
        Integer paidAmount = (Integer) portOnePayment.get("amount");
        String merchantUid = (String) portOnePayment.get("merchant_uid");

        if (!"ready".equalsIgnoreCase(status) && !"paid".equalsIgnoreCase(status)) {
            log.error("[가상계좌 검증 실패] 포트원 결제 상태가 'ready' 또는 'paid'가 아님: {}", status);
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_READY_OR_PAID);
        }
        if (!paidAmount.equals(amount)) {
            log.error("[가상계좌 검증 실패] 결제 금액 불일치. 요청 금액: {}, 실제 금액: {}", amount, paidAmount);
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (!merchantUid.equals(uid)) {
            log.error("[가상계좌 검증 실패] 상점 UID 불일치. 요청 UID: {}, 실제 UID: {}", uid, merchantUid);
            throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }
    }
}
