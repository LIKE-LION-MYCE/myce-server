package com.myce.member.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.dto.MileageUpdateRequest;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import com.myce.member.service.MemberMileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberMileageServiceImpl implements MemberMileageService {
  private final MemberRepository memberRepository;

  @Transactional
  @Override
  public void updateMileageForReservation(Long memberId, MileageUpdateRequest request) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(()-> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

    member.updateMileage(member.getMileage() - request.getUsedMileage() + request.getSavedMileage());
  }

}
