package com.myce.reservation.service.Impl;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.entity.Guest;
import com.myce.member.entity.Member;
import com.myce.member.repository.GuestRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.reservation.dto.ReserverInfo;
import com.myce.reservation.dto.ResolveReserversRequest;
import com.myce.reservation.dto.ResolveReserversResponse;
import com.myce.reservation.dto.ResolvedReserverInfo;
import com.myce.reservation.service.ReserverResolveService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserverResolveServiceImpl implements ReserverResolveService {
  private final GuestRepository guestRepository;

  // 예매자 내역 채우기
  @Override
  @Transactional
  public ResolveReserversResponse resolve(ResolveReserversRequest request) {
    List<ReserverInfo> reserverInfos = Optional.ofNullable(request.getReserverInfos())
        .orElseThrow(() -> new CustomException(CustomErrorCode.RESERVER_INFO_NOT_FOUND));
    if (reserverInfos.isEmpty()) {
      throw new CustomException(CustomErrorCode.RESERVER_INFO_NOT_FOUND);
    }

    // 회원인지 비회원인지 확인
    CustomUserDetails principal = currentUserOrNull();
    boolean ownerIsMember = (principal != null);
    Long ownerMemberId = ownerIsMember ? principal.getMemberId() : null;  // 회원 아이디

    // 첫 번째(예매자) 처리 => 로그인 상태면 MEMBER, 아니면 GUEST
    ReserverInfo owner = reserverInfos.get(0);
    List<ResolvedReserverInfo> resolved = new ArrayList<>();

    if(ownerIsMember){
      // 첫 번째는 무조건 MEMBER. 나머지는 무조건 GUEST.
      resolved.add(ResolvedReserverInfo.builder()
          .name(owner.getName())
          .email(owner.getEmail())
          .phone(owner.getPhone())
          .birth(owner.getBirth())
          .gender(owner.getGender())
          .userType("MEMBER")
          .memberId(ownerMemberId)
          .build());
    } else {
      // 비회원 예매이면 GUEST 처리
      Guest guest = upsertGuest(owner);

      // 있으면 그냥 그대로.
      resolved.add(ResolvedReserverInfo.builder()
          .name(owner.getName())
          .email(owner.getEmail())
          .phone(owner.getPhone())
          .birth(owner.getBirth())
          .gender(owner.getGender())
          .userType("GUEST")
          .guestId(guest.getId())
          .build());
    }

    // 회원자가 2개 이상의 티켓을 예매한 경우
    if(reserverInfos.size() > 1){
      List<ReserverInfo> companions = reserverInfos.subList(1, reserverInfos.size());

      List<String> companionEmails = companions.stream()
          .map(r -> normEmail(r.getEmail()))
          .toList();

      Map<String, Guest> guestsByEmail = guestRepository.findByEmailIn(companionEmails).stream()
          .collect(Collectors.toMap(g -> normEmail(g.getEmail()), g -> g));

      for (ReserverInfo r : companions) {
        String key = normEmail(r.getEmail());
        Guest g = guestsByEmail.get(key);
        if (g == null) {
          g = upsertGuest(r); // 생성
          guestsByEmail.put(key, g);
        }
        resolved.add(ResolvedReserverInfo.builder()
            .name(r.getName())
            .email(r.getEmail())
            .phone(r.getPhone())
            .birth(r.getBirth())
            .gender(r.getGender())
            .userType("GUEST")
            .guestId(g.getId())
            .build());
      }


    }
    return ResolveReserversResponse.builder()
        .reserverInfos(resolved)
        .build();
  }

  // 회원 비회원 구별
  private CustomUserDetails currentUserOrNull() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) return null;
    Object p = auth.getPrincipal();
    return (p instanceof CustomUserDetails) ? (CustomUserDetails) p : null;
  }

  // 이메일 표준화
  private String normEmail(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
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
