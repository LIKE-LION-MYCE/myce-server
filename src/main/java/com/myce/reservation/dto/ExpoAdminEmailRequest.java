package com.myce.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExpoAdminEmailRequest {

    @NotEmpty(message = "수신자는 비어있을 수 없습니다.")
    private List<String> recipients;

    @NotBlank(message = "제목 입력은 필수입니다.")
    private String subject;

    @NotBlank(message = "내용 입력은 필수입니다.")
    private String content;
}
