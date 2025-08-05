package com.myce.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {

    // 회원 M
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "M001", "회원정보가 존재하지 않습니다."),

    // QR 코드 Q
    QR_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "QR 코드를 찾을 수 없습니다."),
    QR_ALREADY_EXISTS(HttpStatus.CONFLICT, "Q002", "이미 QR 코드가 발급된 예약자입니다."),
    QR_ALREADY_USED(HttpStatus.BAD_REQUEST, "Q003", "이미 사용된 QR 코드입니다."),
    QR_EXPIRED(HttpStatus.BAD_REQUEST, "Q004", "만료된 QR 코드입니다."),
    QR_INVALID_STATUS(HttpStatus.BAD_REQUEST, "Q005", "ACTIVE 상태의 QR만 재발급 가능합니다."),
    QR_UNAUTHORIZED(HttpStatus.FORBIDDEN, "Q006", "해당 박람회의 관리자만 처리할 수 있습니다."),
    RESERVER_NOT_FOUND(HttpStatus.NOT_FOUND, "Q007", "예약자를 찾을 수 없습니다."),
    QR_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Q008", "QR 코드 생성 중 오류가 발생했습니다."),
    QR_REISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Q009", "QR 코드 재발급 중 오류가 발생했습니다.");



    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
