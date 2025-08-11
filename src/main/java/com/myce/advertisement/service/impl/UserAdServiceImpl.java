package com.myce.advertisement.service.impl;

import com.myce.advertisement.dto.AdRegistrationRequest;
import com.myce.advertisement.entity.AdPosition;
import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.repository.AdPositionRepository;
import com.myce.advertisement.repository.AdRepository;
import com.myce.advertisement.service.UserAdService;
import com.myce.advertisement.service.mapper.AdRegistrationMapper;
import com.myce.common.dto.RegistrationCompanyRequest;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.service.mapper.BusinessProfileMapper;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAdServiceImpl implements UserAdService {
  private final MemberRepository memberRepository;
  private final AdPositionRepository adPositionRepository;
  private final AdRepository adRepository;
  private final BusinessProfileRepository  businessProfileRepository;

  @Override
  public void saveAdvertisement(Long memberId, AdRegistrationRequest request) {
    // 로그인한 사용자
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

    // 광고 위치
    AdPosition adPosition = adPositionRepository.findById(request.getAdPositionId())
        .orElseThrow(() -> new CustomException(CustomErrorCode.AD_POSITION_NOT_EXIST));

    // 총 등록일 수 구하기
    LocalDate startDate = request.getDisplayStartDate();
    LocalDate endDate = request.getDisplayEndDate();
    long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

    // 광고 객체 생성
    Advertisement advertisement = AdRegistrationMapper.toEntity(request, member, adPosition, (int)totalDays);

    // 광고 등록(저장)
    adRepository.save(advertisement);

    // 등록 신청한 회사 정보 저장
    RegistrationCompanyRequest company = request.getRegistrationCompanyRequest();

    BusinessProfile businessProfile = BusinessProfileMapper.toEntity(company, TargetType.ADVERTISEMENT, advertisement.getId());
    businessProfileRepository.save(businessProfile);
  }
}
