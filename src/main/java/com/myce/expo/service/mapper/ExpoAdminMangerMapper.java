package com.myce.expo.service.mapper;

import com.myce.expo.dto.ExpoAdminManagerResponse;
import com.myce.expo.entity.AdminCode;
import org.springframework.stereotype.Component;

@Component
public class ExpoAdminMangerMapper {
    public ExpoAdminManagerResponse toDto(AdminCode adminCode){
        return ExpoAdminManagerResponse.builder()
                .id(adminCode.getId())
                .adminCode(adminCode.getCode())
                .isExpoDetailUpdate(adminCode.getAdminPermission().getIsExpoDetailUpdate())
                .isBoothInfoUpdate(adminCode.getAdminPermission().getIsBoothInfoUpdate())
                .isScheduleUpdate(adminCode.getAdminPermission().getIsScheduleUpdate())
                .isReserverListView(adminCode.getAdminPermission().getIsReserverListView())
                .isPaymentView(adminCode.getAdminPermission().getIsPaymentView())
                .isEmailLogView(adminCode.getAdminPermission().getIsEmailLogView())
                .isOperationsConfigUpdate(adminCode.getAdminPermission().getIsOperationsConfigUpdate())
                .isInquiryView(adminCode.getAdminPermission().getIsInquiryView())
                .build();
    }
}