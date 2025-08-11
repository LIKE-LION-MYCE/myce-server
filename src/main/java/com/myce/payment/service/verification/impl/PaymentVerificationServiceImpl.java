package com.myce.payment.service.verification.impl;

import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdRepository;
import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.ExpoRepository;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.UserIdentifier;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.payment.service.mapper.PaymentMapper;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.payment.service.verification.PaymentVerificationService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.system.entity.AdFeeSetting;
import com.myce.system.entity.ExpoFeeSetting;
import com.myce.system.repository.AdFeeSettingRepository;
import com.myce.system.repository.ExpoFeeSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVerificationServiceImpl implements PaymentVerificationService {

    private final PortOneApiService portOneApiService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ReservationRepository reservationRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final AdRepository adRepository;
    private final ExpoRepository expoRepository;
    private final AdFeeSettingRepository adFeeSettingRepository;
    private final ExpoFeeSettingRepository expoFeeSettingRepository;

    // 카드 결제 검증 및 저장
    @Override
    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
        String accessToken = portOneApiService.getAccessToken();
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid(), accessToken);
        Integer paidAmount = verifyPaymentDetails(request, portOnePayment);
        identifyUser(request);
        Payment payment = paymentMapper.toEntity(request, portOnePayment);
        paymentRepository.save(payment);
        Object paymentInfo = savePaymentInfoDetails(request, paidAmount, payment, PaymentStatus.SUCCESS);
        return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
    }

    // 가상계좌 발급 및 상태 저장 PENDING
    @Override
    @Transactional
    public PaymentVerifyResponse verifyVbankPayment(PaymentVerifyRequest request) {
        String accessToken = portOneApiService.getAccessToken();
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid(), accessToken);
        Integer paidAmount = verifyVbankDetails(request, portOnePayment);
        identifyUser(request);
        Payment payment = paymentMapper.toEntity(request, portOnePayment);
        paymentRepository.save(payment);
        Object paymentInfo = savePaymentInfoDetails(request, paidAmount, payment, PaymentStatus.PENDING);
        return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
    }

    // 가상계좌 검증
    private Integer verifyVbankDetails(PaymentVerifyRequest request, Map<String, Object> portOnePayment) {
        String status = (String) portOnePayment.get("status");
        Integer paidAmount = (Integer) portOnePayment.get("amount");
        String merchantUid = (String) portOnePayment.get("merchant_uid");

        if (!"ready".equalsIgnoreCase(status) && !"paid".equalsIgnoreCase(status)) {
            log.error("[가상계좌 검증 실패] 포트원 결제 상태가 'ready' 또는 'paid'가 아님: {}", status);
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_READY_OR_PAID);
        }
        if (!paidAmount.equals(request.getAmount())) {
            log.error("[가상계좌 검증 실패] 결제 금액 불일치. 요청 금액: {}, 실제 금액: {}", request.getAmount(), paidAmount);
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (!merchantUid.equals(request.getMerchantUid())) {
            log.error("[가상계좌 검증 실패] 상점 UID 불일치. 요청 UID: {}, 실제 UID: {}", request.getMerchantUid(), merchantUid);
            throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }
        return paidAmount;
    }

    // 결제 사용자 식별
    private UserIdentifier identifyUser(PaymentVerifyRequest request) {
        UserType userType = null;
        Long userId = null;

        switch (request.getTargetType()) {
            case RESERVATION:
                Reservation reservation = reservationRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
                userType = reservation.getUserType();
                userId = reservation.getUserId();
                break;
            case AD:
            case EXPO:
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()
                        || authentication instanceof AnonymousAuthenticationToken) {
                    throw new CustomException(CustomErrorCode.REFRESH_TOKEN_NOT_EXIST);
                }
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userType = UserType.MEMBER;
                userId = userDetails.getMemberId();
                break;
            default:
                throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
        }
        return UserIdentifier.builder().userType(userType).userId(userId).build();
    }

    // 카드 결제 검증
    private Integer verifyPaymentDetails(PaymentVerifyRequest request,
                                         Map<String, Object> portOnePayment) {
        String status = (String) portOnePayment.get("status");
        Integer paidAmount = (Integer) portOnePayment.get("amount");
        String merchantUid = (String) portOnePayment.get("merchant_uid");

        if (!"paid".equalsIgnoreCase(status)) {
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_PAID);
        }
        if (!paidAmount.equals(request.getAmount())) {
            throw new CustomException(CustomErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (!merchantUid.equals(request.getMerchantUid())) {
            throw new CustomException(CustomErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }
        return paidAmount;
    }

    // 결제 대상별 세부 결제 정보 저장(Reservation, Ad, Expo)
    private Object savePaymentInfoDetails(PaymentVerifyRequest request, Integer paidAmount,
                                          Payment payment, PaymentStatus paymentStatus) {
        Object savedPaymentInfo;

        switch (request.getTargetType()) {
            case RESERVATION:
                Reservation reservation = reservationRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
                ReservationPaymentInfo reservationPaymentInfo = paymentMapper.toReservationPaymentInfo(request,
                        reservation, paidAmount, paymentStatus);
                savedPaymentInfo = reservationPaymentInfoRepository.save(reservationPaymentInfo);
                break;
            case AD:
                Advertisement advertisement = adRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.AD_NOT_FOUND));
                AdFeeSetting adFeeSetting = adFeeSettingRepository.findByAdPositionIdAndIsActiveTrue(
                                advertisement.getAdPosition().getId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));
                AdPaymentInfo adPaymentInfo = paymentMapper.toAdPaymentInfo(advertisement, adFeeSetting,
                        paidAmount, paymentStatus);
                savedPaymentInfo = adPaymentInfoRepository.save(adPaymentInfo);
                break;
            case EXPO:
                Expo expo = expoRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
                ExpoFeeSetting expoFeeSetting = expoFeeSettingRepository.findByIsActiveTrue()
                        .orElseThrow(() -> new CustomException(CustomErrorCode.FEE_SETTING_NOT_FOUND));
                ExpoPaymentInfo expoPaymentInfo = paymentMapper.toExpoPaymentInfo(expo, expoFeeSetting,
                        paidAmount, paymentStatus);
                savedPaymentInfo = expoPaymentInfoRepository.save(expoPaymentInfo);
                break;
            default:
                throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
        }
        return savedPaymentInfo;
    }
}
