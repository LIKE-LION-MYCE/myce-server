package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExpoAdminPermissionResponse {
    private List<Long> expoIds;
    private Boolean isExpoDetailUpdate;
    private Boolean isBoothInfoUpdate;
    private Boolean isScheduleUpdate;
    private Boolean isReserverListView;
    private Boolean isPaymentView;
    private Boolean isEmailLogView;
    private Boolean isOperationsConfigUpdate;
    private Boolean isInquiryView;
}