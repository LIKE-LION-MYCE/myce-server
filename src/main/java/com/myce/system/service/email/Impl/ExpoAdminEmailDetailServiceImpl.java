package com.myce.system.service.email.Impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.system.dto.email.ExpoAdminEmailResponse;
import com.myce.system.repository.EmailLogRepository;
import com.myce.system.service.email.ExpoAdminEmailDetailService;
import com.myce.system.service.email.mapper.ExpoAdminEmailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpoAdminEmailDetailServiceImpl implements ExpoAdminEmailDetailService {

    private final ExpoRepository expoRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final EmailLogRepository emailLogRepository;
    private final ExpoAdminEmailMapper mapper;

    @Override
    public Page<ExpoAdminEmailResponse> getMyMails(Long expoId,
                                                   Long memberId,
                                                   LoginType loginType,
                                                   String keyword,
                                                   Pageable pageable) {

        validateMyAccess(expoId,memberId,loginType);

        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if(!hasKeyword){
            return emailLogRepository
                    .findByExpoId(expoId, pageable)
                    .map(mapper::toDto);
        }

        String safeKeyword = java.util.regex.Pattern.quote(keyword.trim());
        return emailLogRepository
                .searchByExpoIdAndKeyword(expoId,safeKeyword,pageable)
                .map(mapper::toDto);
    }

    @Override
    public ExpoAdminEmailResponse getMyMailDetail(Long expoId, Long memberId, LoginType loginType, String emailId) {
        validateMyAccess(expoId, memberId, loginType);

        return emailLogRepository.findById(emailId)
                .map(mapper::toDto)
                .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_EMAIL_LOG));
    }

    private void validateMyAccess(Long expoId, Long memberId, LoginType loginType) {
        if(memberId == null || loginType == null){
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        switch(loginType){
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                if(!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsEmailLogViewTrue(memberId, expoId)){
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}
