package com.myce.advertisement.entity.type;

import java.util.List;

public enum AdvertisementStatus {
    PENDING_APPROVAL,
    PENDING_PAYMENT,// 관리자 승인 대기
    PENDING_PUBLISH,
    PENDING_CANCEL,//
    PUBLISHED,         // 게시 중
    COMPLETED,             // 게시 종료
    REJECTED,
    CANCELLED;

    public static AdvertisementStatus fromString(String text) {
        for (AdvertisementStatus t : AdvertisementStatus.values()) {
            if (t.name().equalsIgnoreCase(text))return t;
        }
        return null;
    }

    public static final List<AdvertisementStatus> COMPLETED_STATUSES = List.of(
            COMPLETED, CANCELLED
    );

    public static final List<AdvertisementStatus> EXPIRED_STATUSES = List.of(
            REJECTED, CANCELLED
    );

    public static final List<AdvertisementStatus> ACTIVE_STATUSES = List.of(
            PUBLISHED, PENDING_CANCEL
    );

    public static final List<AdvertisementStatus> ADMIN_VIEWABLE_STATUSES = List.of(
            PENDING_PUBLISH, PUBLISHED, PENDING_CANCEL, COMPLETED, CANCELLED
    );
}
