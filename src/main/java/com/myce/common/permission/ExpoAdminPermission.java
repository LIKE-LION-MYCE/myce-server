package com.myce.common.permission;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;

public enum ExpoAdminPermission {
    /*
        DB 필드명을 기준으로 작성했습니다.
        UPDATE, VIEW 와 상관없이 탭별, 즉 페이지 기준 권한으로 정의합니다.
     */
    EXPO_DETAIL_UPDATE,
    BOOTH_INFO_UPDATE,
    SCHEDULE_UPDATE,
    OPERATIONS_CONFIG_UPDATE,
    RESERVER_LIST_VIEW,
    PAYMENT_VIEW,
    EMAIL_LOG_VIEW,
    INQUIRY_VIEW;

    public static ExpoAdminPermission fromValue(String value) {
        for (ExpoAdminPermission p : ExpoAdminPermission.values()) {
            if (p.name().equalsIgnoreCase(value)) return p;
        }
        throw new CustomException(CustomErrorCode.INVALID_EXPO_ADMIN_PERMISSION_TYPE);
    }
}