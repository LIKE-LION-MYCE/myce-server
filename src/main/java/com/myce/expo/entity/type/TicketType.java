package com.myce.expo.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketType {
    EARLY_BIRD("얼리버드"),
    GENERAL("일반");

    private final String label;

    public static TicketType fromString(String type) {
        for (TicketType t : TicketType.values()) {
            if (t.name().equalsIgnoreCase(type)) return t;
        }
        return null;
    }
}