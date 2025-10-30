package com.myce.payment.service.verification.impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.service.MemberAdService;
import com.myce.member.service.MemberExpoService;
import com.myce.notification.service.NotificationService;
import com.myce.payment.dto.PaymentInfoDetailDto;
import com.myce.payment.dto.PaymentVerifyInfo;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.UserIdentifier;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.entity.ExpoPaymentInfo;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.payment.service.constant.PortOneResponseKey;
import com.myce.payment.service.impl.PaymentCommonService;
import com.myce.payment.service.impl.VerifyPaymentService;
import com.myce.payment.service.mapper.PaymentMapper;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.payment.service.verification.PaymentVerificationService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MemberExpoService memberExpoService;
    private final MemberAdService memberAdService;
    private final NotificationService notificationService;
    private final VerifyPaymentService verifyPaymentService;
    private final PaymentCommonService paymentCommonService;

    // 카드 결제 검증 및 저장
    @Override
    @Transactional
    public PaymentVerifyResponse verifyPayment(PaymentVerifyInfo verifyInfo) {
        // 결제 정보 검증
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(verifyInfo.getImpUid());
        verifyPaymentService.verifyPaymentDetails(portOnePayment, verifyInfo.getAmount(), verifyInfo.getMerchantUid());

        UserIdentifier userIdentifier = identifyUser(verifyInfo.getTargetType(), verifyInfo.getTargetId());
        if(userIdentifier == null) throw new CustomException(CustomErrorCode.INVALID_PAYMENT_TARGET_TYPE);

        Payment payment = savePayment(portOnePayment, verifyInfo);
        PaymentInfoDetailDto paymentInfo = savePaymentInfoDetails(verifyInfo, PaymentStatus.SUCCESS, userIdentifier);
        return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
    }

    // 가상계좌 발급 및 상태 저장 PENDING
    @Override
    @Transactional
    public PaymentVerifyResponse verifyVbankPayment(PaymentVerifyInfo verifyInfo) {
        // 결제 정보 검증
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(verifyInfo.getImpUid());
        int paidAmount = verifyInfo.getAmount();
        verifyPaymentService.verifyVbankDetails(portOnePayment, paidAmount, verifyInfo.getMerchantUid());

        UserIdentifier userIdentifier = identifyUser(verifyInfo.getTargetType(), verifyInfo.getTargetId());
        Payment payment = paymentMapper.toEntity(verifyInfo, portOnePayment);
        paymentRepository.save(payment);

        PaymentInfoDetailDto paymentInfo = savePaymentInfoDetails(verifyInfo, PaymentStatus.PENDING, userIdentifier);
        return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
    }

    // 결제 사용자 식별
    private UserIdentifier identifyUser(PaymentTargetType targetType, Long targetId) {
        return switch (targetType) {
            case RESERVATION -> getUserIdentifierForReservation(targetId);
            case AD, EXPO -> getUserIdentifierToAuthentication();
        };
    }

    private UserIdentifier getUserIdentifierForReservation(long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        return UserIdentifier.builder().userType(reservation.getUserType()).userId(reservation.getUserId()).build();
    }

    private UserIdentifier getUserIdentifierToAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new CustomException(CustomErrorCode.REFRESH_TOKEN_NOT_EXIST);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return UserIdentifier.builder().userType(UserType.MEMBER).userId(userDetails.getUserId()).build();
    }

    private Payment savePayment(Map<String, Object> portOnePayment, PaymentVerifyInfo request) {
        String payMethod = (String) portOnePayment.get(PortOneResponseKey.PAY_METHOD);
        Payment payment = null;
        if(PaymentMethod.CARD.getName().equalsIgnoreCase(payMethod)) {
            payment = paymentMapper.toEntity(request, portOnePayment);
        } else{
            payment = paymentMapper.toEntityTransfer(request, portOnePayment);
        }

        return paymentRepository.save(payment);
    }

    // 결제 대상별 세부 결제 정보 저장(Reservation, Ad, Expo)
    private PaymentInfoDetailDto savePaymentInfoDetails(
            PaymentVerifyInfo verifyInfo, PaymentStatus paymentStatus, UserIdentifier userIdentifier) {
        Long memberId = userIdentifier.getUserId();
        return switch (verifyInfo.getTargetType()) {
            case RESERVATION -> {
                ReservationPaymentInfo paymentInfo = saveReservationPaymentInfo(verifyInfo, paymentStatus);
                yield new PaymentInfoDetailDto(
                        paymentInfo.getStatus().name(), paymentInfo.getTotalAmount(),
                        paymentInfo.getReservation().getId());
            }
            case AD -> {
                AdPaymentInfo paymentInfo = saveAdPaymentInfo(verifyInfo, paymentStatus, memberId);
                yield new PaymentInfoDetailDto(paymentInfo.getStatus().name(), paymentInfo.getTotalAmount());
            }
            case EXPO -> {
                ExpoPaymentInfo paymentInfo = saveExpoPaymentInfo(verifyInfo, paymentStatus, memberId);
                yield new PaymentInfoDetailDto(paymentInfo.getStatus().name(), paymentInfo.getTotalAmount());
            }
        };
    }

    private ReservationPaymentInfo saveReservationPaymentInfo(
            PaymentVerifyInfo verifyInfo, PaymentStatus paymentStatus) {
        // 예약 정보 조회
        Reservation reservation = reservationRepository.findById(verifyInfo.getTargetId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        // 비회원 적림금 지급 방지 추가
        if (reservation.getUserType().equals(UserType.GUEST)) verifyInfo.setSavedMileage(0);

        int paidAmount = verifyInfo.getAmount();
                // PaymentInfo 생성 후 저장
        ReservationPaymentInfo reservationPaymentInfo = paymentMapper.toReservationPaymentInfo(
                reservation, paidAmount, paymentStatus, verifyInfo.getUsedMileage(), verifyInfo.getSavedMileage());
        reservationPaymentInfo = reservationPaymentInfoRepository.save(reservationPaymentInfo);

        // 결제 완료 시 알림 발송 (회원만)
        if (paymentStatus.equals(PaymentStatus.SUCCESS) && reservation.getUserType().equals(UserType.MEMBER)) {
            paymentCommonService.sendAlert(reservation, paidAmount);
        }

        return reservationPaymentInfo;
    }

    private AdPaymentInfo saveAdPaymentInfo(
            PaymentVerifyInfo verifyInfo, PaymentStatus paymentStatus, Long memberId) {
        // 이미 PaymentInfo 있으므로 SUCCESS로만
        AdPaymentInfo adPaymentInfo = adPaymentInfoRepository.findByAdvertisementId(verifyInfo.getTargetId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        adPaymentInfo.updateStatus(paymentStatus);
        adPaymentInfo = adPaymentInfoRepository.save(adPaymentInfo);

        // completeAdvertisementPayment 호출
        memberAdService.completeAdvertisementPayment(memberId, verifyInfo.getTargetId());
        return adPaymentInfo;
    }

    private ExpoPaymentInfo saveExpoPaymentInfo(
            PaymentVerifyInfo verifyInfo, PaymentStatus paymentStatus, Long memberId) {
        // 이미 PaymentInfo 있으므로 SUCCESS로만
        ExpoPaymentInfo expoPaymentInfo = expoPaymentInfoRepository.findByExpoId(verifyInfo.getTargetId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));
        expoPaymentInfo.updateStatus(paymentStatus);
        expoPaymentInfo = expoPaymentInfoRepository.save(expoPaymentInfo);


        // completeExpoPayment 호출
        memberExpoService.completeExpoPayment(memberId, verifyInfo.getTargetId());
        return expoPaymentInfo;
    }
}
