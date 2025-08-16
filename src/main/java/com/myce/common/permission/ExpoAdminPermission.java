package com.myce.common.permission;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;

public enum ExpoAdminPermission {
    /*
        DB 필드명을 기준으로 작성했습니다.
        UPDATE, VIEW 와 상관없이 탭별, 즉 페이지 기준 권한으로 정의합니다.
     */
    EXPO_DETAIL_UPDATE, //박람회 상세
    BOOTH_INFO_UPDATE, //참가 부스
    SCHEDULE_UPDATE, //행사 일정
    PAYMENT_VIEW, //예약 내역
    RESERVER_LIST_VIEW, //예약자 리스트
    EMAIL_LOG_VIEW, //이메일 전송 이력
    OPERATIONS_CONFIG_UPDATE, //운영 설정
    INQUIRY_VIEW; //문의

    public static ExpoAdminPermission fromValue(String value) {
        for (ExpoAdminPermission p : ExpoAdminPermission.values()) {
            if (p.name().equalsIgnoreCase(value)) return p;
        }
        throw new CustomException(CustomErrorCode.INVALID_EXPO_ADMIN_PERMISSION_TYPE);
    }
}