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
    CATEGORY_NOT_EXIST(HttpStatus.NOT_FOUND, "E002", "카테고리가 존재하지 않습니다."),

    // 티켓 T
    TICKET_NOT_EXIST(HttpStatus.NOT_FOUND, "T001", "티켓이 존재하지 않습니다."),
    TICKET_NOT_BELONG_TO_EXPO(HttpStatus.FORBIDDEN, "T002", "해당 티켓은 현재 박람회에 속하지 않습니다."),
    TICKET_TYPE_INVALID(HttpStatus.BAD_REQUEST, "T003", "유효하지 않은 티켓 타입입니다."),
    
  
    // 관계자 정보 I
    BUSINESS_NOT_EXIST(HttpStatus.NOT_FOUND,"I001", "관계자 정보를 찾지 못했습니다"),

    // 채팅 C
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C002", "채팅방에 접근할 권한이 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "메시지를 찾을 수 없습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "C004", "이미 존재하는 채팅방입니다."),
    CHAT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "채팅 참여자 정보를 찾을 수 없습니다.");

    // 결제 P

    // 예약 R

    // 정산 S

    // 환경 Y

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
