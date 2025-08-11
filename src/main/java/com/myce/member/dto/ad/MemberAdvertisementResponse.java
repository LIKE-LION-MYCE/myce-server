package com.myce.member.dto.ad;

import com.myce.advertisement.entity.type.AdvertisementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAdvertisementResponse {
    
    private Long advertisementId;
    private String title;
    private LocalDate displayStartDate;
    private LocalDate displayEndDate;
    private AdvertisementStatus status;
    private Long adPositionId;
    private String adPositionName;
}