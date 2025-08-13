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
    INVALID_LOGIN_TYPE(HttpStatus.BAD_REQUEST, "U004", "잘못된 로그인 방식입니다."),
    EXPIRED_VERIFICATION_TIME(HttpStatus.BAD_REQUEST, "U005", "인증 시간이 초과되었습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "U006", "인증 코드가 유효하지 않습니다."),
    INVALID_VERIFICATION_TYPE(HttpStatus.BAD_REQUEST, "U007", "유효하지 않는 인증 타입입니다."),

    // 회원 M
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "M001", "회원정보가 존재하지 않습니다."),
    MEMBER_TYPE_INVALID(HttpStatus.NOT_FOUND, "M002", "존재하지 않는 회원 타입입니다."),
    MEMBER_SETTING_NOT_EXIST(HttpStatus.NOT_FOUND, "M003", "회원의 시스템 설정이 존재하지 않습니다."),
    CURRENT_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "M004", "기존 비밀번호가 일치하지 않습니다."),
    PASSWORD_CONFIRMATION_MISMATCH(HttpStatus.BAD_REQUEST, "M005", "새로운 비밀번호가 일치하지 않습니다."),
    GENDER_TYPE_INVALID(HttpStatus.BAD_REQUEST, "M006", "유효하지 않은 성별 값입니다."),
    ALREADY_EXIST_LOGIN_ID(HttpStatus.BAD_REQUEST, "M007", "이미 존재하는 아이디입니다."),
    ALREADY_EXIST_EMAIL(HttpStatus.BAD_REQUEST, "M008", "이미 존재하는 이메일입니다."),

    // 비회원 G
    GUEST_NOT_EXIST(HttpStatus.NOT_FOUND, "G001", "비회원 정보가 존재하지 않습니다."),

    // 박람회 관리자 EAD
    ADMIN_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "EAD001", "존재하지 않는 박람회 관리자 코드입니다."),

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
    QR_APPROVED(HttpStatus.BAD_REQUEST, "Q010", "QR 코드 발급 기간이 아닙니다."),
    QR_NOT_MANUAL_CHECK_IN(HttpStatus.BAD_REQUEST, "Q011", "ACTIVE 상태의 QR만 수기입장 처리가 가능합니다."),

    // S3 S
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "S3 파일 업로드에 실패했습니다."),

    // 엑스포 E
    EXPO_NOT_EXIST(HttpStatus.NOT_FOUND, "E001", "운영중인 박람회가 존재하지 않습니다."),
    EXPO_NOT_FOUND(HttpStatus.NOT_FOUND, "E002", "박람회를 찾을 수 없습니다."),
    EXPO_ACCESS_DENIED(HttpStatus.FORBIDDEN, "E003", "해당 박람회에 대한 접근 권한이 없습니다."),
    CATEGORY_NOT_EXIST(HttpStatus.NOT_FOUND, "E004", "카테고리가 존재하지 않습니다."),
    INVALID_EXPO_STATUS(HttpStatus.NOT_FOUND, "E005", "영수증을 조회 할 수 없습니다."),


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
    AD_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "광고가 존재 하지 않습니다"),
    AD_MAX_CAPACITY_REACHED(HttpStatus.CONFLICT, "A002", "신청할 수 없는 기간이 포함되어 있습니다."),
    INVALID_ADVERTISEMENT_STATUS(HttpStatus.BAD_REQUEST, "A003", "유효하지 않은 광고 상태입니다."),

    // 부스 B
    BOOTH_PREMIUM_RANK_REQUIRED(HttpStatus.BAD_REQUEST, "B001", "프리미엄 부스는 노출 순위가 필수입니다."),
    BOOTH_PREMIUM_RANK_INVALID(HttpStatus.BAD_REQUEST, "B002", "프리미엄 부스 노출 순위는 1에서 3 사이여야 합니다."),
    BOOTH_PREMIUM_MAX_CAPACITY_REACHED(HttpStatus.CONFLICT, "B003", "프리미엄 부스는 최대 3개까지만 등록할 수 있습니다."),
    BOOTH_PREMIUM_RANK_DUPLICATED(HttpStatus.CONFLICT, "B004", "이미 사용중인 프리미엄 부스 노출 순위입니다."),
    BOOTH_NOT_FOUND(HttpStatus.NOT_FOUND, "B005", "부스를 찾을 수 없습니다."),
    BOOTH_NOT_BELONG_TO_EXPO(HttpStatus.FORBIDDEN, "B006", "해당 박람회에 속한 부스가 아닙니다."),
    BOOTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "B007", "부스에 접근할 권한이 없습니다."),

    // 이벤트 EV
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EV001", "이벤트를 찾을 수 없습니다."),
    EVENT_NOT_BELONG_TO_EXPO(HttpStatus.FORBIDDEN, "EV002", "해당 박람회에 속한 이벤트가 아닙니다."),
    EVENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "EV003", "해당 행사에 대한 권한이 없습니다."),


    // 광고 위치 AP
    AD_POSITION_NOT_EXIST(HttpStatus.NOT_FOUND, "AP001", "배너 위치 정보가 존재하지 않습니다."),

    // 결제 P
    PAYMENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "P001", "유효하지 않은 결제 상태값입니다."),
    PAYMENT_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "결제 정보를 찾을 수 없습니다."),
    PORTONE_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "포트원 결제내역 응답이 없습니다."),
    PAYMENT_NOT_PAID(HttpStatus.CONFLICT, "P004", "결제가 완료되지 않았습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "P005", "결제 금액이 다릅니다."),
    PAYMENT_MERCHANT_UID_MISMATCH(HttpStatus.BAD_REQUEST, "P006", "주문번호가 일치하지 않습니다."),
    PORTONE_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "P007", "포트원 인증에 실패했습니다."),
    INVALID_PAYMENT_TARGET_TYPE(HttpStatus.BAD_REQUEST, "P008", "유효하지 않은 결제 타겟입니다."),
    PORTONE_REFUND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P009", "포트원 환불 요청에 실패했습니다."),
    REFUND_AMOUNT_EXCEEDS_PAID(HttpStatus.BAD_REQUEST, "P010", "환불 금액이 결제 금액을 초과합니다."),
    PORTONE_REQUEST_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P011", "포트원 요청 본문 직렬화에 실패했습니다."),
    PORTONE_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "P012", "포트원 요청에 실패했습니다."),
    PAYMENT_NOT_READY_OR_PAID(HttpStatus.BAD_REQUEST, "P013", "결제 상태가 'ready' 또는 'paid'가 아닙니다."),
    WEBHOOK_DATA_MISMATCH(HttpStatus.BAD_REQUEST, "P014", "웹훅 데이터와 포트원 조회 데이터가 일치하지 않습니다."),
    INVALID_MERCHANT_UID_FORMAT(HttpStatus.BAD_REQUEST, "P015", "유효하지 않은 상점 주문번호 형식입니다."),

    // 예약 R
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "해당 예약 정보를 조회 할수 없습니다."),
    RESERVATION_STATUS_INVALID(HttpStatus.BAD_REQUEST, "R002", "유효하지 않은 예약 상태값입니다."),

    // 정산 S
    FEE_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "F001" , "요금 설정이 없습니다"),

    // 환경 Y

    // 거부사유 RJ
    REJECT_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "RJ001", "거부 사유가 존재하지 않습니다."),

    // 환불 RF
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "RF001", "환불 정보가 존재하지 않습니다."),

    // 시스템 설정 에러
    NOT_EXIST_MESSAGE_TEMPLATE(HttpStatus.NOT_FOUND, "SY001", "메시지 템플릿이 존재하지 않습니다."),
    NOT_EXIST_AD_FEE_SETTING(HttpStatus.NOT_FOUND, "SY002", "광고 요금제가 존재하지 않습니다."),
    NOT_EXIST_EXPO_FEE_SETTING(HttpStatus.NOT_FOUND, "SY003", "박람회 요금제가 존재하지 않습니다."),
    ALREADY_SET_ACTIVATION(HttpStatus.BAD_REQUEST, "SY004", "이미 설정되어있는 활성화 값입니다."),

    // 엑셀 EX
    EXCEL_EXPORT_FAILED(HttpStatus.NOT_FOUND, "EX001", "엑셀 추출에 실패하였습니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}