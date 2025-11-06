package com.myce.payment.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.expo.service.TicketService;
import com.myce.payment.dto.PaymentInfoDetailDto;
import com.myce.payment.dto.PaymentVerifyInfo;
import com.myce.payment.dto.PaymentVerifyResponse;
import com.myce.payment.dto.ReservationPaymentVerifyRequest;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.payment.service.ReservationPaymentService;
import com.myce.payment.service.constant.PortOneResponseKey;
import com.myce.payment.service.mapper.PaymentMapper;
import com.myce.payment.service.portone.PortOneApiService;
import com.myce.reservation.dto.GuestReservationRequest;
import com.myce.reservation.dto.PreReservationCacheDto;
import com.myce.reservation.dto.ReserverBulkSaveRequest;
import com.myce.reservation.dto.ReserverInfo;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.PreReservationRepository;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.service.GuestReservationService;
import com.myce.reservation.service.ReservationService;
import com.myce.reservation.service.ReserverService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PaymentCommonService paymentCommonService;
    private final GuestReservationService guestReservationService;
    private final VerifyPaymentService verifyPaymentService;

    @Override
    @Transactional
    public PaymentVerifyResponse verifyAndCompleteReservationPayment(ReservationPaymentVerifyRequest request) {
        log.info("박람회 결제 통합 처리 시작 - reservationId: {}", request.getTargetId());
        
        // 1. 기존 결제 검증 로직
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid());
        int paidAmount = request.getAmount();
        verifyPaymentService.verifyPaymentDetails(portOnePayment, paidAmount, request.getMerchantUid());
        
        // 2. Redis에서 결제 세션 검증 및 DB 저장 => Redis에서 결제 세션 검증 (세션 ID 필수)
        String sessionId = request.getSessionId();
        Reservation reservation = saveReservationToSessionId(sessionId);

        UserType userType = reservation.getUserType();
        long reservationId = reservation.getId();
        
        // 3. 비회원 적립금 지급 방지
        if (userType.equals(UserType.GUEST)) request.setSavedMileage(0);

        try {
            PaymentVerifyInfo verifyInfo = convertToPaymentVerifyInfo(request, reservationId);
            // 4. Payment 엔티티 저장
            Payment payment = savePayment(portOnePayment, verifyInfo);
            
            // 5. ReservationPaymentInfo 저장
            ReservationPaymentInfo paymentInfo =
                    saveReservationPayment(verifyInfo, reservation, paidAmount, PaymentStatus.SUCCESS);
            
            // 6. 예약 상태를 CONFIRMED로 변경
            reservationService.updateStatusToConfirm(reservationId);
            
            // 7. 예약자 정보 저장 및 비회원 Guest ID 생성
            if (request.getReserverInfos() != null && !request.getReserverInfos().isEmpty()) {
                saveReservers(reservationId, request.getReserverInfos(), userType);
            }
            
            // 8. 티켓 수량 감소
            if (request.getTicketId() != null && request.getQuantity() != null) {
                ticketService.updateRemainingQuantity(request.getTicketId(), request.getQuantity());
            }
            
            // 9. 마일리지 처리 & 회원 등급 업데이트(회원만)
            Long userId = reservation.getUserId();
            if (userType.equals(UserType.MEMBER)) {
                // 가상계좌 입금 완료 시 마일리지 및 등급 업데이트 (회원만)
                int usedMileage = Objects.requireNonNullElse(request.getUsedMileage(), 0);
                int savedMileage = Objects.requireNonNullElse(request.getSavedMileage(), 0);
                paymentCommonService.processMileage(usedMileage, savedMileage, userId);
            }
            
            // 11. QR 코드 생성 시도 (실패해도 계속 진행)
            paymentCommonService.issueQrForReservation(reservation);
            
            // 12. 결제 완료 알림 발송
            // 회원: 기존 사이트 내 알림도 유지
            if (userType.equals(UserType.MEMBER)) {
                paymentCommonService.sendAlert(reservation, paidAmount);
            }
            
            // 회원/비회원 공통: 이메일 알림 (첫 번째 예약자에게만 발송)
            if (request.getReserverInfos() != null && !request.getReserverInfos().isEmpty()) {
                ReserverBulkSaveRequest.ReserverSaveInfo reserverInfo = request.getReserverInfos().getFirst();
                paymentCommonService.sendEmail(reservation, reserverInfo, paidAmount);
            }
            
            // 13. Redis에서 결제 세션 정리
            destroyPreReservation(sessionId, reservationId);
            
            log.info("박람회 결제 통합 처리 완료 - reservationId: {}", reservation.getId());
            return paymentMapper.toPaymentVerifyResponse(payment,
                    new PaymentInfoDetailDto(paymentInfo.getStatus().name(), paidAmount, reservationId));
        } catch (Exception e) {
            log.error("박람회 결제 통합 처리 실패 - 오류: {}", e.getMessage(), e);
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_PAID);
        }
    }

    @Override
    @Transactional
    public PaymentVerifyResponse verifyAndPendingVbankReservationPayment(ReservationPaymentVerifyRequest request) {
        log.info("박람회 가상계좌 결제 통합 처리 시작 - reservationId: {}", request.getTargetId());
        
        // 1. 기존 가상계좌 검증 로직
        Map<String, Object> portOnePayment = portOneApiService.getPaymentInfo(request.getImpUid());
        int paidAmount = request.getAmount();
        verifyPaymentService.verifyVbankDetails(portOnePayment, paidAmount, request.getMerchantUid());
        
        // 2. Redis에서 결제 세션 검증 및 DB 저장 => Redis에서 결제 세션 검증 (세션 ID 필수)
        String sessionId = request.getSessionId();
        Reservation reservation = saveReservationToSessionId(sessionId);
        log.info("Redis 세션에서 DB 저장 완료 - reservationId: {}", reservation.getId());

        UserType userType = reservation.getUserType();
        long reservationId = reservation.getId();
        
        // 3. 비회원 적립금 지급 방지
        if (userType.equals(UserType.GUEST)) request.setSavedMileage(0);
        
        try {
            // 4. Payment 엔티티 저장 (PENDING 상태)
            PaymentVerifyInfo paymentRequest = convertToPaymentVerifyInfo(request, reservation.getId());
            //TODO 넘어오는 paymentMethod 확인해서 savePayment메소드 호출하기
            Payment payment = paymentMapper.toEntity(paymentRequest, portOnePayment);
            paymentRepository.save(payment);
            
            // 5. ReservationPaymentInfo 저장 (PENDING 상태)
            ReservationPaymentInfo paymentInfo =
                    saveReservationPayment(paymentRequest, reservation, paidAmount, PaymentStatus.PENDING);
            
            // 6. 예약자 정보 저장 및 비회원 Guest ID 생성
            if (request.getReserverInfos() != null && !request.getReserverInfos().isEmpty()) {
                saveReservers(reservationId, request.getReserverInfos(), userType);
            }
            
            // 7. 티켓 수량 감소
            if (request.getTicketId() != null && request.getQuantity() != null) {
                ticketService.updateRemainingQuantity(request.getTicketId(), request.getQuantity());
            }
            
            // 8. Redis에서 결제 세션 정리
            destroyPreReservation(sessionId, reservationId);
            
            // 가상계좌는 입금 완료 시 웹훅에서 나머지 처리 (마일리지, 등급, QR 등)
            log.info("박람회 가상계좌 결제 처리 완료 - reservationId: {}", reservation.getId());
            return paymentMapper.toPaymentVerifyResponse(payment,
                    new PaymentInfoDetailDto(paymentInfo.getStatus().name(), paidAmount, reservationId));
        } catch (Exception e) {
            log.error("박람회 가상계좌 결제 처리 실패 - reservationId: {}", reservation.getId(), e);
            throw new CustomException(CustomErrorCode.PAYMENT_NOT_READY_OR_PAID);
        }
    }
    
    private PaymentVerifyInfo convertToPaymentVerifyInfo(
            ReservationPaymentVerifyRequest request, Long actualReservationId) {
        PaymentVerifyInfo verifyInfo = new PaymentVerifyInfo();
        verifyInfo.setImpUid(request.getImpUid());
        verifyInfo.setMerchantUid(request.getMerchantUid());
        verifyInfo.setAmount(request.getAmount());
        verifyInfo.setTargetType(request.getTargetType());
        verifyInfo.setTargetId(actualReservationId); // 실제 DB에 저장된 reservation ID 사용
        verifyInfo.setUsedMileage(request.getUsedMileage());
        verifyInfo.setSavedMileage(request.getSavedMileage());
        return verifyInfo;
    }

    private Reservation getNewReservation(PreReservationCacheDto PreReservationInfo) {
        Expo expo = expoRepository.findById(PreReservationInfo.getExpoId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
        Ticket ticket = ticketRepository.findById(PreReservationInfo.getTicketId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        return Reservation.builder()
                .expo(expo)
                .ticket(ticket)
                .reservationCode(PreReservationInfo.getReservationCode())
                .userType(PreReservationInfo.getUserType())
                .userId(PreReservationInfo.getUserId())
                .quantity(PreReservationInfo.getQuantity())
                .status(ReservationStatus.CONFIRMED_PENDING)
                .build();
    }

    private Reservation saveReservationToSessionId(String sessionId) {
        if (sessionId == null) {
            log.error("세션 ID가 제공되지 않음");
            throw new CustomException(CustomErrorCode.PAYMENT_SESSION_EXPIRED);
        }

        // PreReservationRepositoryImpl 경로값 위에 import로 분리
        PreReservationCacheDto reservationCacheDto = preReservationRepository.findBySessionId(sessionId);
        if (reservationCacheDto == null) {
            log.error("결제 세션 만료 또는 유효하지 않음 - 세션 ID: {}", sessionId);
            throw new CustomException(CustomErrorCode.PAYMENT_SESSION_EXPIRED);
        }

        log.info("세션 ID로 Redis 조회 성공: {}", sessionId);
        Reservation newReservation = getNewReservation(reservationCacheDto);
        return reservationRepository.save(newReservation);
    }

    private Payment savePayment(Map<String, Object> portOnePayment, PaymentVerifyInfo verifyInfo) {
        String payMethod = (String) portOnePayment.get(PortOneResponseKey.PAY_METHOD);
        Payment payment;
        if (PaymentMethod.CARD.getName().equalsIgnoreCase(payMethod)) {
            payment = paymentMapper.toEntity(verifyInfo, portOnePayment);
        } else {
            payment = paymentMapper.toEntityTransfer(verifyInfo, portOnePayment);
        }
        return paymentRepository.save(payment);
    }

    private ReservationPaymentInfo saveReservationPayment(
            PaymentVerifyInfo verifyInfo, Reservation reservation, int paidAmount, PaymentStatus paymentStatus) {
        int usedMileage = verifyInfo.getUsedMileage();
        int savedMileage = verifyInfo.getSavedMileage();
        ReservationPaymentInfo paymentInfo = paymentMapper.toReservationPaymentInfo(
                reservation, paidAmount, paymentStatus, usedMileage, savedMileage);
        return reservationPaymentInfoRepository.save(paymentInfo);
    }

    private void saveReservers(long reservationId,
            List<ReserverBulkSaveRequest.ReserverSaveInfo> reserverInfos, UserType userType) {
        reserverService.saveReservers(reservationId, reserverInfos);

        // 비회원인 경우 Guest 엔티티 생성 및 reservation의 userId 업데이트
        if (userType.equals(UserType.GUEST)) {
            GuestReservationRequest guestRequest = GuestReservationRequest.builder()
                    .reservationId(reservationId)
                    .reserverInfos(convertReserverInfoList(reserverInfos))
                    .build();

            guestReservationService.updateGuestId(guestRequest);
            log.info("비회원 Guest ID 생성 및 업데이트 완료 - reservationId: {}", reservationId);
        }
    }

    private List<ReserverInfo> convertReserverInfoList(List<ReserverBulkSaveRequest.ReserverSaveInfo> reserverInfos) {
        return reserverInfos.stream().parallel()
                .map(info -> ReserverInfo.builder()
                        .name(info.getName())
                        .email(info.getEmail())
                        .phone(info.getPhone())
                        .phone(info.getPhone())
                        .birth(info.getBirth())
                        .gender(info.getGender())
                        .build()).collect(java.util.stream.Collectors.toList());
    }

    private void destroyPreReservation(String sessionId, long reservationId) {
        try {
            preReservationRepository.deleteBySessionId(sessionId);
            log.info("Redis 세션 정리 완료 - sessionId: {}, reservationId: {}", sessionId, reservationId);
        } catch (Exception e) {
            log.warn("Redis 세션 정리 실패 - sessionId: {}, reservationId: {}", sessionId, reservationId, e);
        }
    }
}