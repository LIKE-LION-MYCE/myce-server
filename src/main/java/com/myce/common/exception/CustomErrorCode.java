package com.myce.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    // 회원 M
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "M001", "아이디 혹은 비밀번호를 확인해주세요");

    // 엑스포 E


    // 결제 P

    // 예약 R


    // 정산 S

    // 환경 Y

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
