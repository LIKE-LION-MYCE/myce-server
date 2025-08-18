package com.myce.member.service;

import com.myce.member.dto.MileageUpdateRequest;

public interface MemberMileageService {
  void updateMileageForReservation(Long userId, MileageUpdateRequest request);
  
  // 예매 환불 시 마일리지 복원/차감
  void revertMileageForReservationRefund(Long memberId, MileageUpdateRequest request);
}
