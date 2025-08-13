package com.myce.dashboard.dto.platform.type;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PeriodType {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    SIX_MONTHLY("6months"),
    YEARLY("yearly");

    private final String label;

    public static PeriodType fromLabel(String type) {
        for (PeriodType t : PeriodType.values()) {
            if (t.getLabel().equalsIgnoreCase(type)) return t;
        }
        throw new CustomException(CustomErrorCode.PERIOD_TYPE_NOT_EXIST);
    }

    public static long getNumberOfDays(PeriodType type) {
        return switch (type) {
            case DAILY -> 1;
            case WEEKLY -> 7;
            case MONTHLY -> 30;
            case SIX_MONTHLY -> 180;
            case YEARLY -> 365;
        };
    }
}
