package com.myce.reservation.entity.code;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {
    MEMBER("회원"),
    GUEST("비회원");

    private final String label;

    public static UserType fromValue(String value) {
        for (UserType type : UserType.values()) {
            if (type.name().equalsIgnoreCase(value)) return type;
        }
        throw new CustomException(CustomErrorCode.MEMBER_TYPE_INVALID);
    }

    public static UserType fromLabel(String label) {
        for (UserType type : UserType.values()) {
            if (type.getLabel().equals(label)) return type;
        }
        throw new CustomException(CustomErrorCode.MEMBER_TYPE_INVALID);
    }
}
