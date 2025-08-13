package com.myce.reservation.dto;

import com.myce.member.entity.type.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolvedReserverInfo {
  private String name;
  private String email;
  private String phone;
  private LocalDate birth;
  private Gender gender;
  private String userType; // "MEMBER" or "GUEST"
  private Long memberId;   // userType == MEMBER일 때 채움
  private Long guestId;    // userType == GUEST일 때 채움
}
