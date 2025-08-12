package com.myce.member.entity.type;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("남"),
    FEMALE("여");

    private final String label;

    public static Gender fromString(String value) {
        for (Gender genderEnum : Gender.values()) {
            if (genderEnum.toString().equals(value)) {
                return genderEnum;
            }
        }
        throw new CustomException(CustomErrorCode.GENDER_TYPE_INVALID);
    }

    public static Gender fromLabel(String label){
        for (Gender genderEnum : Gender.values()) {
            if(genderEnum.getLabel().equals(label)){
                return genderEnum;
            }
        }
        throw new CustomException(CustomErrorCode.GENDER_TYPE_INVALID);
    }

    public static String toLabel(Gender gender){
        if(gender==null){
            throw new CustomException(CustomErrorCode.GENDER_TYPE_INVALID);
        }
        return gender.getLabel();
    }
}