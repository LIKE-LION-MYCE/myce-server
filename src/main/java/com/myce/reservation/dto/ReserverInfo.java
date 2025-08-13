package com.myce.reservation.dto;

import com.myce.member.entity.type.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReserverInfo {
  @NotBlank
  private String name;

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String phone;

  @NotNull
  private LocalDate birth;

  @NotNull
  private Gender gender;
}
