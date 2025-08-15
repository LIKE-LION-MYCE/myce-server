package com.myce.reservation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
public class GuestReservationRequest {
  @NotNull
  private Long reservationId;
  @NotNull @Valid
  private List<ReserverInfo> reserverInfos;
}
