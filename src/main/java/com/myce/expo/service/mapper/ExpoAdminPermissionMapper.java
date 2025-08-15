package com.myce.expo.service.mapper;

import com.myce.expo.dto.ExpoAdminPermissionResponse;
import com.myce.expo.entity.AdminPermission;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpoAdminPermissionMapper {
    public ExpoAdminPermissionResponse toDto(List<Long> expoIds, AdminPermission adminPermission) {
        if (adminPermission == null) {
            return ExpoAdminPermissionResponse.builder()
                    .expoIds(expoIds)
                    .isExpoDetailUpdate(true)
                    .isBoothInfoUpdate(true)
                    .isScheduleUpdate(true)
                    .isReserverListView(true)
                    .isPaymentView(true)
                    .isEmailLogView(true)
                    .isOperationsConfigUpdate(true)
                    .isInquiryView(true)
                    .build();
        }
        return ExpoAdminPermissionResponse.builder()
                .expoIds(expoIds)
                .isExpoDetailUpdate(adminPermission.getIsExpoDetailUpdate())
                .isBoothInfoUpdate(adminPermission.getIsBoothInfoUpdate())
                .isScheduleUpdate(adminPermission.getIsScheduleUpdate())
                .isReserverListView(adminPermission.getIsReserverListView())
                .isPaymentView(adminPermission.getIsPaymentView())
                .isEmailLogView(adminPermission.getIsEmailLogView())
                .isOperationsConfigUpdate(adminPermission.getIsOperationsConfigUpdate())
                .isInquiryView(adminPermission.getIsInquiryView())
                .build();
    }
}