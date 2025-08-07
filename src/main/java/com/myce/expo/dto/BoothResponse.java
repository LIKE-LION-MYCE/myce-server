package com.myce.expo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoothResponse {
    private Long id;
    private String boothNumber;
    private String name;
    private String description;
    private String mainImageUrl;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Boolean isPremium;
    private Integer displayRank;
}
