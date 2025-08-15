package com.myce.member.entity.type;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;

public enum Role {
    PLATFORM_ADMIN, USER, EXPO_ADMIN;

    public static Role fromName(String name) {
        for(Role role : Role.values()) {
            if(role.name().equals(name)) {
                return role;
            }
        }

        throw new CustomException(CustomErrorCode.MEMBER_ROLE_NOT_EXIST);
    }
}
