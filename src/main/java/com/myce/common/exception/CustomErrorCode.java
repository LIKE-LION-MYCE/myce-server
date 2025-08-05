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
    EXPO_NOT_EXIST(HttpStatus.NOT_FOUND, "E001", "운영중인 박람회가 존재하지 않습니다."),

    // 티켓 T
    TICKET_NOT_EXIST(HttpStatus.NOT_FOUND, "T001", "티켓이 존재하지 않습니다."),
    TICKET_NOT_BELONG_TO_EXPO(HttpStatus.FORBIDDEN, "T002", "해당 티켓은 현재 박람회에 속하지 않습니다.");

    // 결제 P

    // 예약 R

    // 정산 S

    // 환경 Y

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
