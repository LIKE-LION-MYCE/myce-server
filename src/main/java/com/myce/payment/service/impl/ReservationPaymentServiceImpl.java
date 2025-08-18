package com.myce.payment.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.TicketQuantityRequest;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.expo.service.TicketService;
import com.myce.member.dto.MileageUpdateRequest;
import com.myce.member.service.MemberGradeService;
import com.myce.member.service.MemberMileageService;
import com.myce.notification.service.NotificationService;
import com.myce.payment.dto.PaymentVerifyRequest;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.ReservationPaymentVerifyRequest;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.payment.service.ReservationPaymentService;
import com.myce.payment.service.mapper.PaymentMapper;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.qrcode.service.QrCodeService;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.dto.PreReservationCacheDto;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.PreReservationRepository;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.service.ReservationService;
import com.myce.reservation.service.ReserverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationPaymentServiceImpl implements ReservationPaymentService {

    private final PortOneApiService portOneApiService;
    private final PaymentRepository paymentRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final ReservationRepository reservationRepository;
    private final PreReservationRepository preReservationRepository;
    private final ExpoRepository expoRepository;
    private final TicketRepository ticketRepository;
    private final PaymentMapper paymentMapper;
    private final ReservationService reservationService;
    private final ReserverService reserverService;
    private final TicketService ticketService;
    private final MemberMileageService memberMileageService;
    private final MemberGradeService memberGradeService;
    private final QrCodeService qrCodeService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PaymentVerifyResponse verifyAndCompleteReservationPayment(ReservationPaymentVerifyRequest request) {
        log.info("박람회 결제 통합 처리 시작 - reservationId: {}", request.getTargetId());
        
        // 1. 기존 결제 검증 로직
        String accessToken = portOneApiService.getAccessToken();
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid(), accessToken);
        Integer paidAmount = verifyPaymentDetails(request, portOnePayment);
        
        // 2. Redis에서 결제 세션 검증 및 DB 저장
        
        // Redis에서 결제 세션 검증 (임시 ID 0으로 확인)
        PreReservationCacheDto cachedDto = preReservationRepository.findById(0L);
        if (cachedDto == null) {
            log.error("결제 세션 만료 또는 유효하지 않음");
            throw new CustomException(CustomErrorCode.PAYMENT_SESSION_EXPIRED);
        }
        
        // Redis DTO에서 엔티티 재조회하여 DB에 저장
        Expo expo = expoRepository.findById(cachedDto.getExpoId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
        Ticket ticket = ticketRepository.findById(cachedDto.getTicketId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));
        
        Reservation newReservation = Reservation.builder()
                .expo(expo)
                .ticket(ticket)
                .reservationCode(cachedDto.getReservationCode())
                .userType(cachedDto.getUserType())
                .userId(cachedDto.getUserId())
                .quantity(cachedDto.getQuantity())
                .status(ReservationStatus.CONFIRMED_PENDING)
                .build();
        
        Reservation reservation = reservationRepository.save(newReservation);
        log.info("Redis 세션에서 DB 저장 완료 - reservationId: {}", reservation.getId());
        
        // 3. 비회원 적립금 지급 방지
        if (reservation.getUserType() == UserType.GUEST) {
            log.info("비회원 예매이므로 적립금을 0으로 설정 ReservationId: {}", reservation.getId());
            request.setSavedMileage(0);
        }
        
        try {
            // 4. Payment 엔티티 저장
            String payMethod = (String) portOnePayment.get("pay_method");
            Payment payment;
            PaymentVerifyRequest paymentRequest = convertToPaymentVerifyRequest(request, reservation.getId());
            if ("card".equals(payMethod)) {
                payment = paymentMapper.toEntity(paymentRequest, portOnePayment);
            } else {
                payment = paymentMapper.toEntityTransfer(paymentRequest, portOnePayment);
            }
            paymentRepository.save(payment);
            
            // 5. ReservationPaymentInfo 저장
            ReservationPaymentInfo paymentInfo = paymentMapper.toReservationPaymentInfo(
                    paymentRequest, reservation, paidAmount, PaymentStatus.SUCCESS);
            reservationPaymentInfoRepository.save(paymentInfo);
            
            // 6. 예약 상태를 CONFIRMED로 변경
            reservationService.updateStatusToConfirm(reservation.getId());
            
            // 7. 예약자 정보 저장
            if (request.getReserverInfos() != null && !request.getReserverInfos().isEmpty()) {
                reserverService.saveReservers(reservation.getId(), request.getReserverInfos());
            }
            
            // 8. 티켓 수량 감소
            if (request.getTicketId() != null && request.getQuantity() != null) {
                TicketQuantityRequest ticketRequest = TicketQuantityRequest.builder()
                        .ticketId(request.getTicketId())
                        .quantity(request.getQuantity())
                        .build();
                ticketService.updateRemainingQuantity(ticketRequest);
            }
            
            // 9. 마일리지 처리 (회원만)
            if (reservation.getUserType() == UserType.MEMBER) {
                Integer usedMileage = request.getUsedMileage() != null ? request.getUsedMileage() : 0;
                Integer savedMileage = request.getSavedMileage() != null ? request.getSavedMileage() : 0;
                MileageUpdateRequest mileageRequest = new MileageUpdateRequest(usedMileage, savedMileage);
                
                if (usedMileage > 0 || savedMileage > 0) {
                    memberMileageService.updateMileageForReservation(reservation.getUserId(), mileageRequest);
                    log.info("마일리지 처리 완료 - 회원ID: {}, 사용: {}, 적립: {}", 
                            reservation.getUserId(), usedMileage, savedMileage);
                }
                
                // 10. 회원 등급 업데이트
                memberGradeService.udpateGrade(reservation.getUserId());
            }
            
            // 11. QR 코드 생성 시도 (실패해도 계속 진행)
            try {
                qrCodeService.issueQrForReservation(reservation.getId());
                log.info("QR 코드 생성 완료 - reservationId: {}", reservation.getId());
            } catch (Exception qrError) {
                log.warn("QR 코드 생성 실패 (스케줄러에서 재시도) - reservationId: {}, 오류: {}", 
                        reservation.getId(), qrError.getMessage());
            }
            
            // 12. 결제 완료 알림 발송 (회원만)
            if (reservation.getUserType() == UserType.MEMBER) {
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
                            reservation.getId(), e.getMessage());
                }
            }
            
            // 13. Redis에서 결제 세션 정리
            try {
                preReservationRepository.delete(0L);
                log.info("결제 완료 후 Redis 세션 정리 완료 - reservationId: {}", reservation.getId());
            } catch (Exception e) {
                log.warn("Redis 세션 정리 실패 - reservationId: {}, 오류: {}", reservation.getId(), e.getMessage());
            }
            
            log.info("박람회 결제 통합 처리 완료 - reservationId: {}", reservation.getId());
            return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
            
        } catch (Exception e) {
            log.error("박람회 결제 통합 처리 실패 - 오류: {}", e.getMessage(), e);
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_PAID);
        }
    }

    @Override
    @Transactional
    public PaymentVerifyResponse verifyAndCompleteVbankReservationPayment(ReservationPaymentVerifyRequest request) {
        log.info("박람회 가상계좌 결제 통합 처리 시작 - reservationId: {}", request.getTargetId());
        
        // 1. 기존 가상계좌 검증 로직
        String accessToken = portOneApiService.getAccessToken();
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid(), accessToken);
        Integer paidAmount = verifyVbankDetails(request, portOnePayment);
        
        // 2. 예약 정보 조회
        Reservation reservation = reservationRepository.findById(request.getTargetId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        
        // 3. 비회원 적립금 지급 방지
        if (reservation.getUserType() == UserType.GUEST) {
            log.info("비회원 예매이므로 적립금을 0으로 설정 ReservationId: {}", reservation.getId());
            request.setSavedMileage(0);
        }
        
        try {
            // 4. Payment 엔티티 저장 (PENDING 상태)
            Payment payment = paymentMapper.toEntity(convertToPaymentVerifyRequest(request), portOnePayment);
            paymentRepository.save(payment);
            
            // 5. ReservationPaymentInfo 저장 (PENDING 상태)
            ReservationPaymentInfo paymentInfo = paymentMapper.toReservationPaymentInfo(
                    convertToPaymentVerifyRequest(request), reservation, paidAmount, PaymentStatus.PENDING);
            reservationPaymentInfoRepository.save(paymentInfo);
            
            // 6. 예약자 정보 저장
            if (request.getReserverInfos() != null && !request.getReserverInfos().isEmpty()) {
                reserverService.saveReservers(request.getTargetId(), request.getReserverInfos());
            }
            
            // 7. 티켓 수량 감소
            if (request.getTicketId() != null && request.getQuantity() != null) {
                TicketQuantityRequest ticketRequest = TicketQuantityRequest.builder()
                        .ticketId(request.getTicketId())
                        .quantity(request.getQuantity())
                        .build();
                ticketService.updateRemainingQuantity(ticketRequest);
            }
            
            // 가상계좌는 입금 완료 시 웹훅에서 나머지 처리 (마일리지, 등급, QR 등)
            
            log.info("박람회 가상계좌 결제 처리 완료 - reservationId: {}", request.getTargetId());
            return paymentMapper.toPaymentVerifyResponse(payment, paymentInfo);
            
        } catch (Exception e) {
            log.error("박람회 가상계좌 결제 처리 실패 - reservationId: {}, 오류: {}", request.getTargetId(), e.getMessage(), e);
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_READY_OR_PAID);
        }
    }
    
    private PaymentVerifyRequest convertToPaymentVerifyRequest(ReservationPaymentVerifyRequest request, Long actualReservationId) {
        PaymentVerifyRequest converted = new PaymentVerifyRequest();
        converted.setImpUid(request.getImpUid());
        converted.setMerchantUid(request.getMerchantUid());
        converted.setAmount(request.getAmount());
        converted.setTargetType(request.getTargetType());
        converted.setTargetId(actualReservationId); // 실제 DB에 저장된 reservation ID 사용
        converted.setUsedMileage(request.getUsedMileage());
        converted.setSavedMileage(request.getSavedMileage());
        return converted;
    }
    
    private PaymentVerifyRequest convertToPaymentVerifyRequest(ReservationPaymentVerifyRequest request) {
        PaymentVerifyRequest converted = new PaymentVerifyRequest();
        converted.setImpUid(request.getImpUid());
        converted.setMerchantUid(request.getMerchantUid());
        converted.setAmount(request.getAmount());
        converted.setTargetType(request.getTargetType());
        converted.setTargetId(request.getTargetId());
        converted.setUsedMileage(request.getUsedMileage());
        converted.setSavedMileage(request.getSavedMileage());
        return converted;
    }
    
    private Integer verifyPaymentDetails(ReservationPaymentVerifyRequest request, Map<String, Object> portOnePayment) {
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
    
    private Integer verifyVbankDetails(ReservationPaymentVerifyRequest request, Map<String, Object> portOnePayment) {
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
}