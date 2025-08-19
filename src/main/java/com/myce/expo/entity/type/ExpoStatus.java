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
    REJECTED("승인 거절"),
    CANCELLED("취소 완료");

    private final String label;
    
    // 신청 관리 페이지에서 표시되는 상태들
    public static final List<ExpoStatus> APPLICATION_STATUSES = List.of(
            PENDING_APPROVAL,
            PENDING_PAYMENT,
            REJECTED
    );
    
    //운영중으로 간주하는 상태 묶음 (현재 관리 페이지에서 표시)
    public static final List<ExpoStatus> ACTIVE_STATUSES = List.of(
            PENDING_PUBLISH,
            PUBLISHED,
            PENDING_CANCEL,
            PUBLISH_ENDED,
            SETTLEMENT_REQUESTED,
            COMPLETED,
            CANCELLED
    );

    //박람회 관리자가 조회 가능한 상태
    public static final List<ExpoStatus> ADMIN_VIEWABLE_STATUSES = List.of(
            PENDING_PUBLISH,
            PUBLISHED,
            PUBLISH_ENDED,
            SETTLEMENT_REQUESTED,
            COMPLETED
    );

    //박람회 관리자가 편집 가능한 상태(편집 가능 상태 ⊂ 조회 가능 상태)
    public static final List<ExpoStatus> ADMIN_EDITABLE_STATUSES = List.of(
            PENDING_PUBLISH,
            PUBLISHED,
            PUBLISH_ENDED,
            SETTLEMENT_REQUESTED
    );

    public static final List<ExpoStatus> COMPLETED_STATUSES = List.of(
            COMPLETED,
            CANCELLED
    );

    public static final List<ExpoStatus> EXPIRED_STATUSES = List.of(
            REJECTED,
            CANCELLED
    );
}