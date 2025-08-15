package com.myce.expo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpoAdminManagerRequest {
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
