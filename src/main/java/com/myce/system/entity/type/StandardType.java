package com.myce.system.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StandardType {
    AFTER_RESERVATION("예매 후"),
    BEFORE_EXPO_START("관람 전");

    private final String description;
}
