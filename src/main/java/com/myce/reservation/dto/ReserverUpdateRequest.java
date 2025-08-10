package com.myce.reservation.dto;

import com.myce.member.entity.type.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserverUpdateRequest {

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotNull(message = "성별은 필수 선택 항목입니다.")
    private Gender gender;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    private String phone;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;
}