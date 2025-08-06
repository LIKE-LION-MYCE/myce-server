package com.myce.member.entity.type;

public enum Gender {
    MALE, FEMALE;

    public static Gender fromString(String gender) {
        for (Gender genderEnum : Gender.values()) {
            if (genderEnum.toString().equals(gender)) {
                return genderEnum;
            }
        }

        return null;
    }
}
