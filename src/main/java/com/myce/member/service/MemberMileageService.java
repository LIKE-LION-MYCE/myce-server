package com.myce.member.service;

import com.myce.member.dto.MileageUpdateRequest;

public interface MemberMileageService {
  void updateMileageForReservation(Long userId, MileageUpdateRequest request);
}
