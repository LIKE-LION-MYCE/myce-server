package com.myce.expo.entity.type;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketType {
    EARLY_BIRD("얼리버드"),
    GENERAL("일반");

    private final String label;

    public static TicketType fromValue(String type) {
        for (TicketType t : TicketType.values()) {
            if (t.name().equalsIgnoreCase(type)) return t;
        }
        throw new CustomException(CustomErrorCode.TICKET_TYPE_INVALID);
    }

    public static TicketType fromLabel(String label) {
        for (TicketType t : TicketType.values()) {
            if (t.getLabel().equals(label)) return t;
        }
        throw new CustomException(CustomErrorCode.TICKET_TYPE_INVALID);
    }
}