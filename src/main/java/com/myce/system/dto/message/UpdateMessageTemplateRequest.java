package com.myce.system.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMessageTemplateRequest {

    @NotBlank(message = "템플릿명을 입력해주세요.")
    private String name;
    @NotBlank(message = "제목을 입력해주세요.")
    private String subject;
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}
