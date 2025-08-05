package com.myce.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    // 회원 M
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "M001", "회원정보가 존재하지 않습니다."),

    // 엑스포 E
    CATEGORY_NOT_EXIST(HttpStatus.NOT_FOUND, "E001", "카테고리가 존재하지 않습니다.");

    // 결제 P

    // 예약 R


    // 정산 S

    // 환경 Y

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
