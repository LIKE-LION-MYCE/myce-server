package com.myce.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {

    // auth U
    REFRESH_TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED, "U001", "토큰 정보가 존재하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "U002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "U003", "이미 만료된 토큰입니다."),

    // 회원 M
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "M001", "회원정보가 존재하지 않습니다."),

    // 관계자 정보 I
    BUSINESS_NOT_EXIST(HttpStatus.NOT_FOUND, "I001", "관계자 정보를 찾지 못했습니다"),
    BUSINESS_NOT_BELONG_TO_EXPO(HttpStatus.FORBIDDEN, "I002", "해당 박람회의 관계자 정보가 아닙니다."),

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
    EXPO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "E003", "해당 박람회에 대한 접근 권한이 없습니다."),
    EXPO_UPDATE_DENIED(HttpStatus.FORBIDDEN, "E004", "해당 박람회에 대한 수정 권한이 없습니다."),

    // 티켓 T
    TICKET_NOT_EXIST(HttpStatus.NOT_FOUND, "T001", "티켓이 존재하지 않습니다."),
    TICKET_NOT_BELONG_TO_EXPO(HttpStatus.FORBIDDEN, "T002", "해당 티켓은 현재 박람회에 속하지 않습니다."),
    TICKET_TYPE_INVALID(HttpStatus.BAD_REQUEST, "T003", "유효하지 않은 티켓 타입입니다."),

    // 채팅 C
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C002", "채팅방에 접근할 권한이 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "메시지를 찾을 수 없습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "C004", "이미 존재하는 채팅방입니다."),
    CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "채팅 참여자 정보를 찾을 수 없습니다."),
    CHAT_SENDER_TYPE_INVALID(HttpStatus.BAD_REQUEST, "C006", "유효하지 않은 메시지 발송자 타입입니다."),

    // 광고 A
    BANNER_NOT_EXIST(HttpStatus.NOT_FOUND, "A001", "배너가 존재하지 않습니다."),
    BANNER_POSITION_NOT_EXIST(HttpStatus.NOT_FOUND, "A002", "배너 위치가 존재하지 않습니다.");
    // 결제 P

    // 예약 R

    // 정산 S

    // 환경 Y

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}