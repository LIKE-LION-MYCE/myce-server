package com.myce.member.dto.expo;

import com.myce.expo.entity.type.ExpoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberExpoResponse {
    
    private Long expoId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private String location;
    private String locationDetail;
    private ExpoStatus status;
    private Boolean isPremium;
}