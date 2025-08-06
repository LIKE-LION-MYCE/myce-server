package com.myce.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
  
    // 회원 M
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "M001", "회원정보가 존재하지 않습니다."),

    // 관계자 정보 I
    BUSINESS_NOT_EXIST(HttpStatus.NOT_FOUND,"I001", "관계자 정보를 찾지 못했습니다"),

    // QR 코드 Q
    QR_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "QR 코드를 찾을 수 없습니다."),
    QR_ALREADY_EXISTS(HttpStatus.CONFLICT, "Q002", "이미 QR 코드가 발급된 예약자입니다."),
    QR_ALREADY_USED(HttpStatus.BAD_REQUEST, "Q003", "이미 사용된 QR 코드입니다."),
    QR_EXPIRED(HttpStatus.BAD_REQUEST, "Q004", "만료된 QR 코드입니다."),
    QR_INVALID_STATUS(HttpStatus.BAD_REQUEST, "Q005", "ACTIVE 상태의 QR만 재발급 가능합니다."),
    QR_UNAUTHORIZED(HttpStatus.FORBIDDEN, "Q006", "해당 박람회의 관리자만 처리할 수 있습니다."),
    RESERVER_NOT_FOUND(HttpStatus.NOT_FOUND, "Q007", "예약자를 찾을 수 없습니다."),
    QR_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Q008", "QR 코드 생성 중 오류가 발생했습니다."),
    QR_REISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Q009", "QR 코드 재발급 중 오류가 발생했습니다."),

    // S3 S
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "S3 파일 업로드에 실패했습니다."),

    // 엑스포 E
    EXPO_NOT_EXIST(HttpStatus.NOT_FOUND, "E001", "운영중인 박람회가 존재하지 않습니다."),
    CATEGORY_NOT_EXIST(HttpStatus.NOT_FOUND, "E002", "카테고리가 존재하지 않습니다."),
    EXPO_ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "E003", "해당 박람회의 관리자가 아닙니다."),

    // 티켓 T
    TICKET_NOT_EXIST(HttpStatus.NOT_FOUND, "T001", "티켓이 존재하지 않습니다."),
    TICKET_NOT_BELONG_TO_EXPO(HttpStatus.FORBIDDEN, "T002", "해당 티켓은 현재 박람회에 속하지 않습니다."),
    TICKET_TYPE_INVALID(HttpStatus.BAD_REQUEST, "T003", "유효하지 않은 티켓 타입입니다.");

    // 결제 P

    // 예약 R

    // 정산 S

    // 환경 Y

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
