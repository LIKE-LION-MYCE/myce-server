package com.myce.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPaymentSummaryResponse {
  private Long ticketId;
  private String ticketName;
  private Integer ticketPrice;
  private Integer ticketQuantity;
}
