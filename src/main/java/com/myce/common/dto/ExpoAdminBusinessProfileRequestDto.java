package com.myce.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpoAdminBusinessProfileRequestDto {
    @Size(max = 500, message = "이미지 URL 길이는 500자 이하여야 합니다.")
    private String logoUrl;

    @NotBlank(message = "회사명은 필수입니다.")
    @Size(max = 100, message = "회사명은 100자 이하여야 합니다.")
    private String companyName;

    @NotBlank(message = "대표명은 필수입니다.")
    @Size(max = 20, message = "대표명은 20자 이하여야 합니다.")
    private String ceoName;

    @NotBlank(message = "대표 이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @Size(max = 100, message = "이메일 길이는 100자 이하여야 합니다.")
    private String contactEmail;

    @NotBlank(message = "대표 연락처는 필수입니다.")
    @Size(max = 13, message = "연락처 길이는 13자 이하여야 합니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 연락처 형식을 입력해주세요.")
    private String contactPhone;

    @NotBlank(message = "회사 주소는 필수입니다.")
    @Size(max = 300, message = "주소 길이는 300자 이하여야 합니다.")
    private String address;

    @NotBlank(message = "사업자 번호는 필수입니다.")
    @Size(max = 50, message = "사업자 번호는 50자 이하여야 합니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 번호 형식이 올바르지 않습니다. (xxx-xx-xxxxx)")
    private String businessRegistrationNumber;
}