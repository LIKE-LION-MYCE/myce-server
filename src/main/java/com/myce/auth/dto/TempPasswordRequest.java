package com.myce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TempPasswordRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
    @NotBlank(message = "아이디를 입력해주세요.")
    private String loginId;
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

}
