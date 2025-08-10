package com.myce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyEmailCodeRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(min = 10, max = 100, message = "이메일은 10자 이상 100자 이하로 입력해주세요.")
    private String email;

    @NotBlank(message = "코드를 입력해주세요.")
    private String code;
}
