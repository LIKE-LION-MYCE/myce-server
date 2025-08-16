package com.myce.reservation.dto;

import com.myce.reservation.entity.code.UserType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreReservationRequest {

  @NotNull
  private Long expoId;

  @NotNull
  private Long ticketId;

  @NotNull
  private UserType userType; // "MEMBER" or "GUEST"

  @NotNull
  private Long userId;

  @Min(1)
  private int quantity;
}
