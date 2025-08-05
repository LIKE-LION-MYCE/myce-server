package com.myce.advertisement.entity.type;

import com.myce.expo.entity.type.TicketType;

public enum AdvertisementStatus {
    PENDING_APPROVAL,
    PENDING_PAYMENT,// 관리자 승인 대기
    PENDING_PUBLISH,
    PENDING_CANCEL,//
    PUBLISHED,         // 게시 중
    COMPLETED,             // 게시 종료
    REJECTED,
    CANCELLED;

    public static TicketType fromString(String type) {
        for (TicketType t : TicketType.values()) {
            if (t.name().equalsIgnoreCase(type)) return t;
        }
        return null;
    }
}
