package com.myce.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPendingResponse {
  private String accountBank;
  private String accountNumber;
  private Integer amount;
  private String dueDate;
}
