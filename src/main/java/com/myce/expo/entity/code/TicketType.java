package com.myce.expo.entity.code;

public enum TicketType {
    EARLY_BIRD, GENERAL;

    public static TicketType fromString(String type) {
        for (TicketType t : TicketType.values()) {
            if (t.name().equalsIgnoreCase(type)) return t;
        }
        return null;
    }
}