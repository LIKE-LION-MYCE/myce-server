package com.myce.reservation.service.Impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.member.entity.Guest;
import com.myce.member.entity.Member;
import com.myce.member.repository.GuestRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.reservation.dto.PreReservationRequest;
import com.myce.reservation.dto.PreReservationResponse;
import com.myce.reservation.dto.ReservationDetailResponse;
import com.myce.reservation.dto.ReservationPendingRequest;
import com.myce.reservation.dto.ReservationSuccessResponse;
import com.myce.reservation.dto.ReserverBulkUpdateRequest;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.service.ReservationCodeService;
import com.myce.reservation.service.mapper.ReservationDetailMapper;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ReservationService;
import com.myce.reservation.service.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final ReserverRepository reserverRepository;
    private final ReservationDetailMapper reservationDetailMapper;
    private final TicketRepository ticketRepository;
    private final ReservationMapper reservationMapper;
    private final ExpoRepository expoRepository;
    private final ReservationCodeService reservationCodeService;
    private final MemberRepository memberRepository;
    private final GuestRepository guestRepository;

    @Override
    public ReservationDetailResponse getReservationDetail(Long reservationId, CustomUserDetails currentUser) {
        Reservation reservation = reservationRepository.findByIdWithExpoAndTicket(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));
        
        // 예약 소유권 검증
        validateReservationOwnership(reservation, currentUser);

        List<Reserver> reservers = reserverRepository.findByReservation(reservation);
        
        return reservationDetailMapper.toResponseDto(reservation, reservers);
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
    public Long saveReservationPending(ReservationPendingRequest request) {
        // 티켓 가져오기
        Ticket ticket = ticketRepository.findById(request.getTicketId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        // 티켓 재고 검증
        if(ticket.getRemainingQuantity() < request.getQuantity()){
            throw new CustomException(CustomErrorCode.TICKET_SOLD_OUT);
        }

        // 예약 엔티티 생성
        String reservationCode = reservationCodeService.generate(request.getExpoId());
        Expo expo = expoRepository.findById(request.getExpoId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        Reservation reservation = reservationMapper.toEntity(expo, ticket, request,
            reservationCode, ReservationStatus.CONFIRMED_PENDING);

        return reservationRepository.save(reservation).getId();
    }

    @Transactional
    @Override
    public void updateStatusToConfirm(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        reservation.updateStatus(ReservationStatus.CONFIRMED);
    }

    @Override
    public ReservationSuccessResponse getReservationCodeAndEmail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

        UserType userType = reservation.getUserType();
        Long userId = reservation.getUserId();

        if(userType == UserType.MEMBER){
            Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
            return reservationMapper.toSuccessResponse(reservation, member.getEmail());
        } else{
            Guest guest = guestRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GUEST_NOT_EXIST));
            return reservationMapper.toSuccessResponse(reservation, guest.getEmail());
        }
    }

    @Transactional
    @Override
    public PreReservationResponse savePreReservation(PreReservationRequest request) {
        // 예약 번호 생성
        String reservationCode = reservationCodeService.generate(request.getExpoId());
        Expo expo = expoRepository.findById(request.getExpoId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));
        Ticket ticket = ticketRepository.findById(request.getTicketId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

        // 엔티티 생성
        Reservation preReservation = reservationMapper.toPreEntity(expo, ticket, request, reservationCode, ReservationStatus.CONFIRMED_PENDING);

        // 저장
        Reservation saved = reservationRepository.save(preReservation);

        // 예약 번호 반환
        return new PreReservationResponse(saved.getId());
    }
}