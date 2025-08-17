package com.myce.auth.dto.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VerificationType {
    SIGNUP("회원가입"),
    FIND_ID("아이디 찾기"),
    FIND_PASSWORD("비밀번호 찾기"),
    NONMEMBER_VERIFY("비회원 인증");

    private final String description;
}
