package com.myce.expo.service.impl;

import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.dto.RegistrationCompanyRequest;
import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.entity.Category;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.service.ExpoService;
import com.myce.common.service.mapper.BusinessProfileMapper;
import com.myce.expo.service.mapper.ExpoMapper;
import com.myce.expo.repository.CategoryRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpoServiceImpl implements ExpoService {
    private final MemberRepository memberRepository;
    private final ExpoRepository expoRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessProfileRepository businessProfileRepository;

    @Override
    public void saveExpo(Long memberId, ExpoRegistrationRequest request) {
        // 로그인한 사용자
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        // expo 객체 생성
        Expo expo = ExpoMapper.toEntity(request, member);

        // 카테고리 추가
        for (Long categoryId : request.getCategoryIds()) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.CATEGORY_NOT_EXIST));

            ExpoCategory expoCategory = ExpoCategory.builder()
                    .category(category)
                    .expo(expo)
                    .build();

            expo.getExpoCategories().add(expoCategory);
        }

        // 박람회 등록(저장)
        Expo savedExpo = expoRepository.save(expo);

        // 등록 신청한 회사 정보 저장
        RegistrationCompanyRequest company = request.getRegistrationCompanyRequest();

        BusinessProfile businessProfile = BusinessProfileMapper.toEntity(company, TargetType.EXPO, savedExpo.getId());

        businessProfileRepository.save(businessProfile);
    }
}
