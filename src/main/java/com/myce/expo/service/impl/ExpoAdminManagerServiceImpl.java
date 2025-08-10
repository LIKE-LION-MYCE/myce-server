package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.ExpoAdminManagerRequest;
import com.myce.expo.dto.ExpoAdminManagerResponse;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.AdminPermission;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.ExpoAdminManagerService;
import com.myce.expo.service.mapper.ExpoAdminMangerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpoAdminManagerServiceImpl implements ExpoAdminManagerService {

    private final AdminCodeRepository adminCodeRepository;
    private final ExpoRepository expoRepository;
    private final ExpoAdminMangerMapper mapper;

    @Override//TODO:하위관리자
    public List<ExpoAdminManagerResponse> getMyExpoManagers(Long expoId, Long memberId) {
        validateMyExpoAccess(expoId, memberId);
        List<AdminCode> adminCodes = adminCodeRepository.findAllWithAdminPermissionByExpoId(expoId);

        return adminCodes.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional//TODO:하위관리자
    public List<ExpoAdminManagerResponse> updateMyExpoManagers(
            Long expoId,
            Long memberId,
            List<ExpoAdminManagerRequest> dtos) {

        validateMyExpoAccess(expoId, memberId);

        List<Long> ids = dtos.stream()
                .map(ExpoAdminManagerRequest::getId)
                .toList();

        List<AdminCode> adminCodes = adminCodeRepository.findAllWithAdminPermissionByIds(ids);

        Map<Long,AdminCode> adminCodeMap = adminCodes.stream()
                .collect(Collectors.toMap(AdminCode::getId,adminCode -> adminCode));

        dtos.forEach(dto ->{
            AdminCode adminCode = adminCodeMap.get(dto.getId());
            if(adminCode!=null){
                AdminPermission permission = adminCode.getAdminPermission();
                permission.updateAdminPermission(
                        dto.getIsExpoDetailUpdate(), dto.getIsBoothInfoUpdate(), dto.getIsScheduleUpdate(),
                        dto.getIsReserverListView(), dto.getIsPaymentView(), dto.getIsEmailLogView(),
                        dto.getIsOperationsConfigUpdate(), dto.getIsSettlementView(), dto.getIsInquiryView()
                );
            }else{
                throw new CustomException(CustomErrorCode.ADMIN_CODE_NOT_FOUND);
            }
        });

        return adminCodes.stream()
                .map(mapper::toDto)
                .toList();
    }

    private void validateMyExpoAccess(Long expoId, Long memberId) {
        if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
    }
}