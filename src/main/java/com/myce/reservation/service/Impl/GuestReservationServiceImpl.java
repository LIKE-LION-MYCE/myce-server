package com.myce.reservation.service.Impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.entity.Guest;
import com.myce.member.repository.GuestRepository;
import com.myce.reservation.dto.ReserverInfo;
import com.myce.reservation.dto.GuestReservationRequest;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.service.GuestReservationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 이 서비스는 프론트엔드에서 받은 '예매자 정보 목록'을 분석하여,
// 각 예매자가 '회원(MEMBER)'인지 '비회원(GUEST)'인지를 판별하고,
// 비회원일 경우 DB에 등록(또는 조회)하여 고유 ID를 부여하는 역할
@Slf4j
@Service
@RequiredArgsConstructor
public class GuestReservationServiceImpl implements GuestReservationService {
  private final GuestRepository guestRepository;
  private final ReservationRepository reservationRepository;

  // 예매자 목록을 받아서 회원/비회원 여부를 판별하고 ID를 부여
  @Override
  @Transactional
  public void updateGuestId(GuestReservationRequest request) {
    List<ReserverInfo> reserverInfos = Optional.ofNullable(request.getReserverInfos())
        .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_INFO_NOT_FOUND));
    if (reserverInfos.isEmpty()) {
      throw new CustomException(CustomErrorCode.RESERVER_INFO_NOT_FOUND);
    }

    // 비회원 예매자 가져오기
    ReserverInfo guestBuyer = reserverInfos.get(0);

    // 비회원 예매이면 GUEST 처리
    // upsertGuest 메서드를 호출하여 DB에 해당 비회원 정보가 있는지 확인
    // 없으면 새로 생성하고, 있으면 기존 정보를 가져옴
    Guest guest = upsertGuest(guestBuyer);
    Long guestId = guest.getId();

    Reservation reservation = reservationRepository.findById(request.getReservationId())
        .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVATION_NOT_FOUND));

    reservation.updateGuestId(guestId);
  }

  // 이메일 조회로 없으면 등록, 있으면 반환
  private Guest upsertGuest(ReserverInfo info) {
    return guestRepository.findByEmail(info.getEmail())
        .orElseGet(() -> {
          Guest g = new Guest();
          g.setEmail(info.getEmail().trim());
          g.setName(info.getName());
          g.setPhone(info.getPhone());
          g.setBirth(info.getBirth());
          g.setGender(info.getGender()); // Guest 엔티티는 String 컬럼 사용
          return guestRepository.save(g);
        });
  }
}
