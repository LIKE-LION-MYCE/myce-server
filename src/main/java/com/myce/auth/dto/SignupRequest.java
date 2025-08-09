package com.myce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하로 입력해주세요.")
    private String name;
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 5, max = 20, message = "아이디는 5자 이상 20자 이하로 입력해주세요.")
    private String loginId;
    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(min = 10, max = 100, message = "이메일은 10자 이상 100자 이하로 입력해주세요.")
    private String email;
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하로 입력해주세요.")
    private String password;
    @NotBlank(message = "생년월일을 입력해주세요.")
    @Size(min = 8, max = 8, message = "생년월일은 8자로만 입력해주세요.(YYYYMMdd)")
    private String birth;
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Size(min = 13, max = 13, message = "전화번호는 13자로 입력해주세요.(000-0000-0000)")
    private String phone;
    @NotBlank(message = "성별을 선택해주세요.")
    private String gender;
}
