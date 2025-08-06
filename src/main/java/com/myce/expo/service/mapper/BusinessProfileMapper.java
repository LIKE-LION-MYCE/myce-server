package com.myce.expo.service.mapper;

import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.expo.dto.ExpoRegistrationCompanyRequest;

public class BusinessProfileMapper {
  public static BusinessProfile toEntity(ExpoRegistrationCompanyRequest company, Long expoId){
    return BusinessProfile.builder()
          .targetType(TargetType.EXPO)
          .targetId(expoId)
          .companyName(company.getCompanyName())
          .address(company.getAddress())
          .ceoName(company.getCeoName())
          .contactPhone(company.getContactPhone())
          .contactEmail(company.getContactEmail())
          .businessRegistrationNumber(company.getBusinessRegistrationNumber())
          .build();
  }
}
