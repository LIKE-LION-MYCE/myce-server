package com.myce.advertisement.entity.type;

public enum AdvertisementStatus {
    PENDING_APPROVAL,  // 관리자 승인 대기
    WAITING_PAYMENT,   // 결제 대기
    WAITING_PUBLISH,   // 게시 대기
    PUBLISHED,         // 게시 중
    ENDED,             // 게시 종료
    REJECTED          // 반려
}
