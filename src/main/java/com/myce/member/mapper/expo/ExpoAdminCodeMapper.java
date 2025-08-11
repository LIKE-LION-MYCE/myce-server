package com.myce.member.mapper.expo;

import com.myce.expo.entity.AdminCode;
import com.myce.member.dto.expo.ExpoAdminCodeResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpoAdminCodeMapper {
    
    public ExpoAdminCodeResponse toExpoAdminCodeResponse(AdminCode adminCode) {
        return ExpoAdminCodeResponse.builder()
                .adminCodeId(adminCode.getId())
                .code(adminCode.getCode())
                .expiredAt(adminCode.getExpiredAt())
                .createdAt(adminCode.getCreatedAt())
                .build();
    }
    
    public List<ExpoAdminCodeResponse> toExpoAdminCodeResponseList(List<AdminCode> adminCodes) {
        return adminCodes.stream()
                .map(this::toExpoAdminCodeResponse)
                .toList();
    }
}