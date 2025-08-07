package com.myce.expo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoothRequest {

    @NotBlank(message = "부스 번호는 필수입니다.")
    @Size(max = 30, message = "부스 번호는 30자 이하여야 합니다.")
    private String boothNumber;

    @NotBlank(message = "부스 이름은 필수입니다.")
    @Size(max = 100, message = "부스 이름은 100자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "부스 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "메인 이미지 URL은 필수입니다.")
    @Size(max = 500, message = "메인 이미지 URL은 500자 이하여야 합니다.")
    private String mainImageUrl;

    @NotBlank(message = "담당자 이름은 필수입니다.")
    @Size(max = 30, message = "담당자 이름은 30자 이하여야 합니다.")
    private String contactName;

    @NotBlank(message = "담당자 연락처는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "유효한 전화번호 형식이 아닙니다.")
    @Size(max = 13, message = "담당자 연락처는 13자 이하여야 합니다.")
    private String contactPhone;

    @NotBlank(message = "담당자 이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "담당자 이메일은 100자 이하여야 합니다.")
    private String contactEmail;

    @NotNull(message = "프리미엄 여부는 필수입니다.")
    private Boolean isPremium;

    private Integer displayRank;
}
