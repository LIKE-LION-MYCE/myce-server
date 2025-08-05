package com.myce.expo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpoRegistrationCompanyRequest {
  private String companyName;   // 회사명
  private String businessRegistrationNumber;  // 사업자번호
  private String address;// 회사 주소
  private String ceoName; // 대표자명
  private String contactPhone; // 대표자 연락처
  private String contactEmail; // 대표자 이메일
}
