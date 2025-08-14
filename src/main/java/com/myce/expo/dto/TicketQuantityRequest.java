package com.myce.expo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketQuantityRequest {
  @NotNull
  private Long ticketId;

  @NotNull
  @Min(1)
  private Integer quantity;
}
