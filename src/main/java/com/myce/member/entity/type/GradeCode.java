package com.myce.member.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GradeCode {
    BRONZE("초보 찍찍이"), SILVER("열정 찍찍이"),
    GOLD("정예 찍찍이"), PLATINUM("엘리트 찍찍이"),
    DIAMOND("레전드 찍찍이");

    private final String name;
}
