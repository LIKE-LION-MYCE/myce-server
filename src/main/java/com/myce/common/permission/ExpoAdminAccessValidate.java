package com.myce.common.permission;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
    박람회 관리자의 api 요청에 대한 조회/편집 권한 검증을 위한 메소드입니다.
 */
@Component
@RequiredArgsConstructor
public class ExpoAdminAccessValidate {

    private final ExpoRepository expoRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    
    //GET 요청에서 사용 : 조회 권한 검증
    public void ensureViewable(Long expoId, Long memberId, LoginType loginType, ExpoAdminPermission permission) {

        //기본 유효성 검사
        if (memberId == null || loginType == null) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }
        if (permission == null) {
            throw new CustomException(CustomErrorCode.INVALID_EXPO_ADMIN_PERMISSION_TYPE);
        }

        //해당 엑스포의 상태가 조회 가능한 상태인지 확인
        ExpoStatus status = expoRepository.findStatusById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        if(!ExpoStatus.ADMIN_VIEWABLE_STATUSES.contains(status)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }
        
        //박람회 관리자가 해당 엑스포에 대한 권한이 있는지 확인
        switch (loginType){
            case MEMBER -> {
                if(!expoRepository.existsByIdAndMemberId(expoId, memberId)){
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }

            case ADMIN_CODE -> {
                boolean allowed = switch(permission){
                    case RESERVER_LIST_VIEW
                            -> adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsReserverListViewTrue(memberId,expoId);
                    case PAYMENT_VIEW
                            -> adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsPaymentViewTrue(memberId,expoId);
                    case EMAIL_LOG_VIEW
                            -> adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsEmailLogViewTrue(memberId,expoId);
                    case INQUIRY_VIEW
                            -> adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsInquiryViewTrue(memberId,expoId);
                    default -> throw new CustomException(CustomErrorCode.INVALID_EXPO_ADMIN_PERMISSION_TYPE);
                };

                if (!allowed) {
                    throw new CustomException(CustomErrorCode.EXPO_ADMIN_PERMISSION_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
    
    //POST, PUT, DELETE 등의 요청에서 사용 : 편집 권한 검증
    public void ensureEditable(Long expoId, Long memberId, LoginType loginType, ExpoAdminPermission permission) {
        
        //기본 유효성 검사
        if (memberId == null || loginType == null) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }
        if (permission == null) {
            throw new CustomException(CustomErrorCode.INVALID_EXPO_ADMIN_PERMISSION_TYPE);
        }

        //해당 엑스포가 편집 가능한 상태인지 확인
        ExpoStatus status = expoRepository.findStatusById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED));

        if(!ExpoStatus.ADMIN_EDITABLE_STATUSES.contains(status)) {
            throw new CustomException(CustomErrorCode.EXPO_EDIT_DENIED);
        }
        
        //박람회 관리자가 해당 엑스포에 대한 편집 권한이 있는지 확인
        switch (loginType) {
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }

            case ADMIN_CODE -> {
                boolean allowed = switch(permission){
                    case EXPO_DETAIL_UPDATE
                            -> adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsExpoDetailUpdateTrue(memberId,expoId);
                    case BOOTH_INFO_UPDATE
                            -> adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsBoothInfoUpdateTrue(memberId,expoId);
                    case SCHEDULE_UPDATE
                            -> adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsScheduleUpdateTrue(memberId,expoId);
                    case OPERATIONS_CONFIG_UPDATE
                            -> adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsOperationsConfigUpdateTrue(memberId,expoId);
                    default -> throw new CustomException(CustomErrorCode.INVALID_EXPO_ADMIN_PERMISSION_TYPE);
                };

                if (!allowed) {
                    throw new CustomException(CustomErrorCode.EXPO_ADMIN_PERMISSION_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}
