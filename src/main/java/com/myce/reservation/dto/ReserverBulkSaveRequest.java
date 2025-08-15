package com.myce.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.myce.member.entity.type.Gender;
import com.myce.reservation.entity.code.UserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReserverBulkSaveRequest {
  @NotNull
  private Long reservationId;

  @NotEmpty
  @Valid
  private List<ReserverSaveInfo> reserverInfos;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReserverSaveInfo {
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull
    private Gender gender;

    @NotBlank
    private String phone;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotNull
    private LocalDate birth;
  }
}
