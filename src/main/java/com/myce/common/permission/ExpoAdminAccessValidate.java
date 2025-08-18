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
    박람회 관리자의 api 요청에 대한 페이지별 조회/편집 권한 검증 메소드입니다.
 */
@Component
@RequiredArgsConstructor
public class ExpoAdminAccessValidate {

    private final ExpoRepository expoRepository;
    private final AdminPermissionRepository adminPermissionRepository;

    //GET 메서드에서 사용 : 조회 권한 검증
    public void ensureViewable(Long expoId, Long memberId, LoginType loginType, ExpoAdminPermission permission) {

        //기본 유효성 검사
        basicValidate(memberId, loginType, permission);

        //해당 엑스포의 상태가 조회 가능한 상태인지 확인
        ExpoStatus status = expoRepository.findStatusById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        if(!ExpoStatus.ADMIN_VIEWABLE_STATUSES.contains(status)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED); //조회 가능한 상태가 아니라면 예외 반환(*)
        }
        
        //박람회 관리자가 해당 엑스포 페이지에 대한 권한이 있는지 확인
        expoAdminValidate(expoId, memberId, loginType, permission);
    }
    
    //PUT, POST, DELETE 등의 메소드에서 사용 : 편집 권한 검증
    public void ensureEditable(Long expoId, Long memberId, LoginType loginType, ExpoAdminPermission permission) {
        
        //기본 유효성 검사
        basicValidate(memberId, loginType, permission);

        //해당 엑스포가 편집 가능한 상태인지 확인
        ExpoStatus status = expoRepository.findStatusById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED));

        if(!ExpoStatus.ADMIN_EDITABLE_STATUSES.contains(status)) {
            throw new CustomException(CustomErrorCode.EXPO_EDIT_DENIED); //편집이 가능한 상태가 아니라면 예외 반환(*)
        }
        
        //박람회 관리자가 해당 엑스포 페이지에 대한 권한이 있는지 확인
        expoAdminValidate(expoId, memberId, loginType, permission);
    }

    private void basicValidate(Long memberId, LoginType loginType, ExpoAdminPermission permission) {
        if (memberId == null || loginType == null) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        if (permission == null) {
            throw new CustomException(CustomErrorCode.INVALID_EXPO_ADMIN_PERMISSION_TYPE);
        }
    }

    // 대시보드용 단순 관리자 권한 체크 (권한별 세부 검증 없이 관리자인지만 확인)
    public void ensureAdmin(Long expoId, Long memberId, LoginType loginType) {
        if (memberId == null || loginType == null) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        // 해당 박람회의 상태가 조회 가능한 상태인지 확인
        ExpoStatus status = expoRepository.findStatusById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        if (!ExpoStatus.ADMIN_VIEWABLE_STATUSES.contains(status)) {
            throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
        }

        // 박람회 관리자인지만 간단히 확인
        switch (loginType) {
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                // 관리 코드로 로그인한 경우, 해당 박람회의 관리 코드인지만 확인
                if (!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoId(memberId, expoId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }

    private void expoAdminValidate(Long expoId, Long memberId, LoginType loginType, ExpoAdminPermission permission) {
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
