package com.myce.advertisement.entity.type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AdvertisementStatus {
    PENDING_APPROVAL,
    PENDING_PAYMENT,// 관리자 승인 대기
    PENDING_PUBLISH,
    PENDING_CANCEL,//
    PUBLISHED,         // 게시 중
    COMPLETED,             // 게시 종료
    REJECTED,
    CANCELLED;

    public static List<String> getNames() {
        return Arrays.stream(AdvertisementStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
