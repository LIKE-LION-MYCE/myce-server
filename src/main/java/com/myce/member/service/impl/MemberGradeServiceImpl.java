package com.myce.member.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.entity.Member;
import com.myce.member.entity.MemberGrade;
import com.myce.member.repository.MemberGradeRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.member.service.MemberGradeService;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberGradeServiceImpl implements MemberGradeService {
  private final MemberRepository memberRepository;
  private final ReservationRepository reservationRepository;
  private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
  private final MemberGradeRepository memberGradeRepository;

  @Transactional
  @Override
  public void udpateGrade(Long memberId) {
    // 회원 정보 조회
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

    // 회원의 '확정된(CONFIRMED)' 상태인 모든 예약 조회
    List<Reservation> confirmedReservations = reservationRepository.findByUserIdAndUserTypeAndStatus(
        memberId, UserType.MEMBER, ReservationStatus.CONFIRMED);

    Integer totalPayAmount = 0;
    // 확정된 예약이 있을 경우에만 총결제 금액 계산
    if (!confirmedReservations.isEmpty()) {
      totalPayAmount = reservationPaymentInfoRepository.findTotalAmountByReservations(confirmedReservations)
          .orElse(0);
    }

    // 모든 등급 정보를 기준 금액(내림차순)으로 조회
    List<MemberGrade> allGrades = memberGradeRepository.findAllByOrderByBaseAmountDesc();
    if (allGrades.isEmpty()) {
      return;
    }

    // 누적 결제 금액에 맞는 새로운 등급 결정
    MemberGrade newGrade = null;
    for (MemberGrade grade : allGrades) {
      if (totalPayAmount >= grade.getBaseAmount()) {
        newGrade = grade; // 조건을 만족하는 가장 높은 등급
        break;
      }
    }

    // 회원의 현재 등급과 새로운 등급이 다를 경우에만 업데이트
    if (!member.getMemberGrade().equals(newGrade)) {
      log.info("회원 등급 업데이트: memberId={}, 이전 등급={}, 새 등급={}, 누적 결제액={}",
          memberId, member.getMemberGrade().getDescription(), newGrade.getDescription(),
          totalPayAmount);
      member.updateMemberGrade(newGrade);
    }
  }
}
