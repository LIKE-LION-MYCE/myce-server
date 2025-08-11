package com.myce.auth.dto;

import com.myce.auth.dto.type.VerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyEmailCodeRequest {

    @NotNull(message = "타입을 입력해주세요.")
    private VerificationType verificationType;

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "코드를 입력해주세요.")
    private String code;
}
