package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
import com.myce.expo.dto.ExpoAdminManagerRequest;
import com.myce.expo.dto.ExpoAdminManagerResponse;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.AdminPermission;
import com.myce.expo.repository.AdminCodeRepository;
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

    private final ExpoAdminAccessValidate expoAdminAccessValidate;
    private final AdminCodeRepository adminCodeRepository;
    private final ExpoAdminMangerMapper mapper;

    @Override
    public List<ExpoAdminManagerResponse> getMyExpoManagers(Long expoId, Long memberId, LoginType loginType) {
        expoAdminAccessValidate.ensureViewable(expoId, memberId, loginType, ExpoAdminPermission.OPERATIONS_CONFIG_UPDATE);
        List<AdminCode> adminCodes = adminCodeRepository.findAllWithAdminPermissionByExpoId(expoId);

        return adminCodes.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ExpoAdminManagerResponse> updateMyExpoManagers(
            Long expoId,
            Long memberId,
            LoginType loginType,
            List<ExpoAdminManagerRequest> dtos) {

        expoAdminAccessValidate.ensureEditable(expoId, memberId, loginType, ExpoAdminPermission.OPERATIONS_CONFIG_UPDATE);

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
                        dto.getIsOperationsConfigUpdate(), dto.getIsInquiryView()
                );
            }else{
                throw new CustomException(CustomErrorCode.ADMIN_CODE_NOT_FOUND);
            }
        });

        return adminCodes.stream()
                .map(mapper::toDto)
                .toList();
    }
}