package com.myce.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpoAdminBusinessProfileResponseDto {
    private String logoUrl;
    private String companyName;
    private String ceoName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String businessRegistrationNumber;
}
