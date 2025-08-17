package com.myce.member.dto;

import com.myce.member.entity.type.Gender;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfoWithMileageResponse {
  private String name;
  private LocalDate birth;
  private String loginId;
  private String phone;
  private String email;
  private Gender gender;
  private BigDecimal mileageRate;
  private String gradeDescription;
  private String gradeImageUrl;
  private Integer mileage;
}

