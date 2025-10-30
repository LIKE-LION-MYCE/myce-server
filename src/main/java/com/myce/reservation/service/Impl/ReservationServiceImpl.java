package com.myce.reservation.service.Impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.member.entity.Guest;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberGrade;
import com.myce.member.repository.GuestRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.repository.PaymentRepository;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.qrcode.service.QrCodeService;
import com.myce.reservation.dto.PreReservationCacheDto;
import com.myce.reservation.dto.PreReservationRequest;
import com.myce.reservation.dto.PreReservationResponse;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReservationPaymentSummaryResponse;
import com.myce.reservation.dto.ReservationPendingResponse;
import com.myce.reservation.dto.ReservationSuccessResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.PreReservationRepository;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ReservationCodeService;
import com.myce.reservation.service.ReservationService;
import com.myce.reservation.service.mapper.ReservationDetailMapper;
import com.myce.reservation.service.mapper.ReservationMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초");
    
    private final ReservationRepository reservationRepository;
    private final ReserverRepository reserverRepository;
    private final ReservationDetailMapper reservationDetailMapper;
    private final TicketRepository ticketRepository;
    private final ReservationMapper reservationMapper;
    private final ExpoRepository expoRepository;
    private final ReservationCodeService reservationCodeService;
    private final MemberRepository memberRepository;
    private final GuestRepository guestRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final PreReservationRepository preReservationRepository;
    private final QrCodeService qrCodeService;

    @Override
    public ReservationDetailResponse getReservationDetail(Long reservationId, CustomUserDetails currentUser) {
        Reservation reservation = reservationRepository.findByIdWithExpoAndTicket(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        
        // 예약 소유권 검증
        validateReservationOwnership(reservation, currentUser);

        List<Reserver> reservers = reserverRepository.findByReservation(reservation);
        
        // 결제 정보 조회
        ReservationPaymentInfo paymentInfo = reservationPaymentInfoRepository.findByReservationId(reservationId).orElse(null);
        Payment payment = paymentRepository.findByTargetIdAndTargetType(reservationId, PaymentTargetType.RESERVATION).orElse(null);
        
        // 회원 등급 정보 조회 (회원인 경우만)
        MemberGrade memberGrade = null;
        if (reservation.getUserType() == UserType.MEMBER) {
            Member member = memberRepository.findById(reservation.getUserId()).orElse(null);
            if (member != null) {
                memberGrade = member.getMemberGrade();
            }
        }
        
        return reservationDetailMapper.toResponseDto(reservation, reservers, paymentInfo, payment, memberGrade);
    }
    
    @Override
    @Transactional
    public void updateReservers(Long reservationId, ReserverBulkUpdateRequest request, CustomUserDetails currentUser) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        
        // 예약 소유권 검증
        validateReservationOwnership(reservation, currentUser);

        List<Reserver> existingReservers = reserverRepository.findByReservation(reservation);
        
        // 기존 예약자들을 ID로 매핑
        Map<Long, Reserver> reserverMap = existingReservers.stream()
                .collect(Collectors.toMap(Reserver::getId, reserver -> reserver));
        
        // 요청된 예약자 정보로 업데이트
        for (ReserverBulkUpdateRequest.ReserverInfo reserverInfo : request.getReserverInfos()) {
            Reserver reserver = reserverMap.get(reserverInfo.getReserverId());
            
            if (reserver == null) {
                throw new CustomException(CustomErrorCode.RESERVER_NOT_FOUND);
            }
            
            reserver.updateReserverInfo(
                reserverInfo.getName(),
                reserverInfo.getGender(),
                reserverInfo.getPhone(),
                reserverInfo.getEmail()
            );
        }
    }

    private void validateReservationOwnership(Reservation reservation, CustomUserDetails currentUser) {
        // LoginType이 MEMBER인 경우만 처리 (일반 회원)
        if (currentUser.getLoginType() != LoginType.MEMBER) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 예약의 UserType과 userId가 현재 사용자와 일치하는지 확인
        if (reservation.getUserType() != UserType.MEMBER ||
            !reservation.getUserId().equals(currentUser.getMemberId())) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
    }

    @Transactional
    @Override
    public void updateStatusToConfirm(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        reservation.updateStatus(ReservationStatus.CONFIRMED);
        
        // 예매 확정 시 QR 코드 즉시 생성 (2일 이내 예매인 경우)
        try {
            qrCodeService.issueQrForReservation(reservationId);
        } catch (Exception e) {
            // QR 생성 실패가 예매 확정에 영향을 주지 않도록 로그만 남김
            // (QR은 스케줄러에서도 생성되므로 백업 메커니즘 존재)
            log.warn("예매 확정 시 QR 코드 즉시 생성 실패 - 예약 ID: {}, 오류: {}", reservationId, e.getMessage());
        }
    }

    @Override
    public ReservationSuccessResponse getReservationCodeAndEmail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        Long userId = reservation.getUserId();
        return switch (reservation.getUserType()) {
            case MEMBER -> {
                Member member = memberRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
                yield reservationMapper.toSuccessResponse(reservation, member.getEmail());
            }
            case GUEST -> {
                Guest guest = guestRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.GUEST_NOT_EXIST));
                yield reservationMapper.toSuccessResponse(reservation, guest.getEmail());
            }
        };
    }

    @Transactional
    @Override
    public PreReservationResponse savePreReservation(PreReservationRequest request) {
        // 예약 번호 생성
        String reservationCode = reservationCodeService.generate(request.getExpoId());
        Long expoId = request.getExpoId();
        if(!expoRepository.existsById(expoId)) throw new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
        Long ticketId = request.getTicketId();
        if(!ticketRepository.existsById(ticketId)) throw new CustomException(CustomErrorCode.TICKET_NOT_EXIST);

        // Redis 캐시용 DTO 생성
        PreReservationCacheDto cacheDto = getPreReservation(request, expoId, ticketId, reservationCode);

        // DB에는 저장하지 않고 Redis에만 10분 TTL로 저장 (모든 예매가 고유한 세션 ID 사용)
        try {
            String sessionId = preReservationRepository.saveWithUniqueKey(cacheDto, 10);
            log.info("결제 세션 Redis 저장 완료 ({}) - 세션 ID: {}, reservationCode: {}",
                    request.getUserType().name(), sessionId, reservationCode);
            
            // 세션 ID와 함께 반환 (reservationId는 0)
            return new PreReservationResponse(0L, sessionId);
        } catch (Exception e) {
            log.error("결제 세션 Redis 저장 실패 - reservationCode: {}", reservationCode, e);
            throw new CustomException(CustomErrorCode.RESERVATION_CODE_GENERATION_FAILED);
        }
    }

    private PreReservationCacheDto getPreReservation(
            PreReservationRequest request, Long expoId, Long ticketId, String reservationCode) {
        return PreReservationCacheDto.builder()
                .expoId(expoId)
                .ticketId(ticketId)
                .reservationCode(reservationCode)
                .userType(request.getUserType())
                .userId(request.getUserId())
                .quantity(request.getQuantity())
                .status(ReservationStatus.CONFIRMED_PENDING)
                .build();
    }

    @Override
    public ReservationPaymentSummaryResponse getPaymentSummary(Long reservationId) {
        log.info("getPaymentSummary 호출 - reservationId: {}", reservationId);

        Long ticketId;
        int ticketQuantity;
        if (reservationId == 0L) {
            // TODO 세션ID 필요 로직 확인하기
            log.info("Redis에서 캐시 데이터 조회 시도 - reservationId: 0 (세션 ID 필요)");
            // 세션 ID가 없으면 기존 방식으로 폴백
            PreReservationCacheDto cachedDto = preReservationRepository.findById(0L);
            if (cachedDto == null) throw new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND);

            log.info("Redis에서 캐시 데이터 조회 성공 - reservationCode: {}", cachedDto.getReservationCode());
            ticketId = cachedDto.getTicketId();
            ticketQuantity = cachedDto.getQuantity();
        } else {
            // 일반적인 DB 조회 (기존 로직)
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
            ticketId = reservation.getTicket().getId();
            ticketQuantity = reservation.getQuantity();
        }

        return getPaymentSummaryWithTicket(ticketId, ticketQuantity);
    }

    private ReservationPaymentSummaryResponse getPaymentSummaryWithTicket(Long ticketId, int ticketQuantity) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));
        // 티켓 타입
        String ticketType = ticket.getType().toString();

        // 티켓 이름
        String ticketName = "[" + ticketType + "] " + ticket.getName();

        return reservationMapper.toPaymentSummary(ticket, ticketName, ticketQuantity);
    }

    @Override
    public ReservationPaymentSummaryResponse getPaymentSummaryBySessionId(String sessionId) {
        log.info("getPaymentSummaryBySessionId 호출 - sessionId: {}", sessionId);
        if (sessionId == null) throw new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND);
        
        PreReservationCacheDto cachedDto = preReservationRepository.findBySessionId(sessionId);
        if (cachedDto == null) throw new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND);
        
        log.info("Redis에서 캐시 데이터 조회 성공 - 세션 ID: {}, reservationCode: {}", 
                sessionId, cachedDto.getReservationCode());

        return getPaymentSummaryWithTicket(cachedDto.getTicketId(), cachedDto.getQuantity());
    }

    @Transactional
    @Override
    public void deletePendingReservation(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    @Override
    public ReservationPendingResponse getVirtualAccountInfo(Long reservationId) {
        Payment payment = paymentRepository.findByTargetIdAndTargetType(reservationId, PaymentTargetType.RESERVATION)
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        ReservationPaymentInfo reservationPaymentInfo = reservationPaymentInfoRepository.findByReservationId(reservationId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_INFO_NOT_FOUND));

        // 내일의 자정 직전 시간을 계산
        // LocalDateTime 객체를 정의된 포맷의 String으로 변환합니다.
        String formattedDueDate = LocalDate.now()
                .plusDays(1).atTime(23, 59, 59)
                .format(DATE_TIME_FORMATTER);

        return reservationMapper.toPendingResponse(payment, reservationPaymentInfo.getTotalAmount(),
            formattedDueDate);
    }

    @Override
    public ReservationDetailResponse getNonMemberReservationDetail(String email, String reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCodeAndEmail(reservationCode, email)
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        Long reservationId = reservation.getId();

        // 예매자 정보 조회
        List<Reserver> reservers = reserverRepository.findByReservationId(reservationId);

        // 결제 정보 조회
        Optional<ReservationPaymentInfo> paymentInfoOptional =
                reservationPaymentInfoRepository.findByReservationId(reservationId);

        // 결제 정보가 없는 경우 (예: 대기 상태)
        if (paymentInfoOptional.isEmpty()) {
            return reservationDetailMapper.toResponseDto(reservation, reservers);
        }

        // 결제 완료된 경우 Payment 정보와 Member 등급 조회
        Payment payment = paymentRepository
                .findByTargetIdAndTargetType(reservationId, PaymentTargetType.RESERVATION)
                .orElse(null);

        MemberGrade memberGrade = null;
        if (reservation.getUserType().equals(UserType.MEMBER)) {
            Member member = memberRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            memberGrade = member.getMemberGrade();
        }

        return reservationDetailMapper.toResponseDto(
                reservation, reservers, paymentInfoOptional.get(), payment, memberGrade);
    }
}