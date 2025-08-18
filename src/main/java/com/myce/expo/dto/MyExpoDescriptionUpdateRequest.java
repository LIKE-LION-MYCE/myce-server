package com.myce.expo.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 나의 박람회 설명 부분 수정 요청 DTO (PENDING_PUBLISH 상태용)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyExpoDescriptionUpdateRequest {

    @Size(max = 2000, message = "설명은 2000자 이하로 입력해주세요.")
    private String description;
}