package com.myce.expo.service;

import com.myce.common.entity.BusinessProfile;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.expo.dto.ExpoRegistrationCompanyRequest;
import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.entity.Category;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import com.myce.expo.service.mapper.BusinessProfileMapper;
import com.myce.expo.service.mapper.ExpoMapper;
import com.myce.expo.repository.CategoryRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpoServiceImpl implements ExpoService {
  private final MemberRepository memberRepository;
  private final ExpoRepository expoRepository;
  private final CategoryRepository  categoryRepository;
  private final BusinessProfileRepository businessProfileRepository;

  @Override
  public void registerExpo(ExpoRegistrationRequest request) {
    // 로그인한 사용자
    Member member = memberRepository.findById(request.getMemberId())
        .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

    // expo 객체 생성
    Expo expo = ExpoMapper.toEntity(request, member);

    // 카테고리 추가
    for (Long categoryId : request.getCategoryIds()) {
      Category category = categoryRepository.findById(categoryId)
          .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

      ExpoCategory expoCategory = ExpoCategory.builder()
          .category(category)
          .expo(expo)
          .build();

      expo.getExpoCategories().add(expoCategory);
    }

    // 박람회 등록(저장)
    Expo registeredExpo = expoRepository.save(expo);

    // 등록 신청한 회사 정보 저장
    ExpoRegistrationCompanyRequest company = request.getExpoRegistrationCompanyRequest();

    BusinessProfile businessProfile = BusinessProfileMapper.toEntity(company, registeredExpo.getId());

    businessProfileRepository.save(businessProfile);
  }
}
