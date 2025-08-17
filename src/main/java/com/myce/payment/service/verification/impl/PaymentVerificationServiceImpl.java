package com.myce.payment.service.verification.impl;

import com.myce.advertisement.repository.AdRepository;
import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.service.MemberAdService;
import com.myce.member.service.MemberExpoService;
import com.myce.notification.service.NotificationService;
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
import com.myce.system.repository.AdFeeSettingRepository;
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
    private final AdFeeSettingRepository adFeeSettingRepository;
    private final MemberExpoService memberExpoService;
    private final MemberAdService memberAdService;
    private final NotificationService notificationService;

    // 카드 결제 검증 및 저장
    @Override
    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyRequest request) {
        String accessToken = portOneApiService.getAccessToken();
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid(), accessToken);
        Integer paidAmount = verifyPaymentDetails(request, portOnePayment);
        UserIdentifier userIdentifier = identifyUser(request);
        String payMethod = (String) portOnePayment.get("pay_method");
        Payment payment = null;
        if(payMethod == "card"){
            payment = paymentMapper.toEntity(request, portOnePayment);
        } else{
            payment = paymentMapper.toEntityTransfer(request, portOnePayment);
        }
        paymentRepository.save(payment);
        Object paymentInfo = savePaymentInfoDetails(request, paidAmount, PaymentStatus.SUCCESS, userIdentifier);
        return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
    }

    // 가상계좌 발급 및 상태 저장 PENDING
    @Override
    @Transactional
    public PaymentVerifyResponse verifyVbankPayment(PaymentVerifyRequest request) {
        String accessToken = portOneApiService.getAccessToken();
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid(), accessToken);
        Integer paidAmount = verifyVbankDetails(request, portOnePayment);
        UserIdentifier userIdentifier = identifyUser(request);
        Payment payment = paymentMapper.toEntity(request, portOnePayment);
        paymentRepository.save(payment);
        Object paymentInfo = savePaymentInfoDetails(request, paidAmount, PaymentStatus.PENDING, userIdentifier);
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
        PaymentStatus paymentStatus, UserIdentifier userIdentifier) {
        Object savedPaymentInfo;
        Long memberId = null;

        switch (request.getTargetType()) {
            case RESERVATION:
                // 예약 정보 조회
                Reservation reservation = reservationRepository.findById(request.getTargetId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

                // 비회원 적림금 지급 방지 추가
                if (reservation.getUserType() == UserType.GUEST) {
                    log.info("비회원 예매이므로 적립금을 0으로 설정 ReservationId: {}", reservation.getId());
                    request.setSavedMileage(0);
                }

                // PaymentInfo 생성 후 저장
                ReservationPaymentInfo reservationPaymentInfo = paymentMapper.toReservationPaymentInfo(request,
                        reservation, paidAmount, paymentStatus);
                savedPaymentInfo = reservationPaymentInfoRepository.save(reservationPaymentInfo);
                
                // 결제 완료 시 알림 발송 (회원만)
                if (paymentStatus == PaymentStatus.SUCCESS && reservation.getUserType() == UserType.MEMBER) {
                    try {
                        String expoTitle = reservation.getExpo().getTitle();
                        String paymentAmount = String.format("%,d원", paidAmount);
                        notificationService.sendPaymentCompleteNotification(
                            reservation.getUserId(),
                            reservation.getId(),
                            expoTitle,
                            paymentAmount
                        );
                        log.info("결제 완료 알림 발송 - 예약 ID: {}, 회원 ID: {}, 금액: {}", 
                                reservation.getId(), reservation.getUserId(), paymentAmount);
                    } catch (Exception e) {
                        log.error("결제 완료 알림 발송 실패 - 예약 ID: {}, 오류: {}", 
                                reservation.getId(), e.getMessage(), e);
                    }
                }
                break;
            case AD:
                // 이미 PaymentInfo 있으므로 SUCCESS로만
                AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(request.getTargetId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
                adPaymentInfo.updateStatus(paymentStatus);
                savedPaymentInfo = adPaymentInfoRepository.save(adPaymentInfo);

                memberId = userIdentifier.getUserId();

                // completeAdvertisementPayment 호출
                memberAdService.completeAdvertisementPayment(memberId, request.getTargetId());
                break;
            case EXPO:
                // 이미 PaymentInfo 있으므로 SUCCESS로만
                ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(request.getTargetId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
                expoPaymentInfo.updateStatus(paymentStatus);
                savedPaymentInfo = expoPaymentInfoRepository.save(expoPaymentInfo);

                // SecurityContext에서 사용자 정보 가져오기
                memberId = userIdentifier.getUserId();

                // completeExpoPayment 호출
                memberExpoService.completeExpoPayment(memberId, request.getTargetId());
                break;
            default:
                throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);
        }
        return savedPaymentInfo;
    }
}
