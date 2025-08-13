package com.myce.reservation.dto;

import com.myce.reservation.entity.code.UserType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class ReservationPendingRequest {
  @NotNull
  private Long expoId;

  @NotNull
  @Min(1)
  private Integer quantity;

  @NotNull
  private Long ticketId;

  @NotNull
  private UserType userType;

  @NotNull
  private Long userId;
}
