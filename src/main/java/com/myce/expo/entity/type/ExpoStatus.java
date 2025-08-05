package com.myce.expo.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ExpoStatus {
    PENDING_APPROVAL("승인 대기"),
    PENDING_PAYMENT("결제 대기"),
    PENDING_PUBLISH("게시 대기"),
    PENDING_CANCEL("취소 대기"),
    PUBLISHED("게시 중"),
    PUBLISH_ENDED("게시 종료"),
    SETTLEMENT_REQUESTED("정산 요청"),
    COMPLETED("종료됨"),
    REJECTED("거절됨"),
    CANCELLED("취소됨");

    private final String label;
    
    //운영중으로 간주하는 상태 묶음
    public static final List<ExpoStatus> ACTIVE_STATUSES = List.of(
            PENDING_PUBLISH,
            PENDING_CANCEL,
            PUBLISHED,
            PUBLISH_ENDED,
            SETTLEMENT_REQUESTED
    );
}
