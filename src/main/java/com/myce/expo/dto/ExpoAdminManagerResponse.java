package com.myce.expo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpoAdminManagerResponse {
    private Long id;
    private String adminCode;
    private Boolean isExpoDetailUpdate;
    private Boolean isBoothInfoUpdate;
    private Boolean isScheduleUpdate;
    private Boolean isReserverListView;
    private Boolean isPaymentView;
    private Boolean isEmailLogView;
    private Boolean isOperationsConfigUpdate;
    private Boolean isInquiryView;
}