package com.myce.expo.dto;

import com.myce.expo.entity.type.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TicketSummaryResponse {
  private Long ticketId;               // 식별자
  private String name;                 // 티켓명
  private TicketType type;             // 티켓 타입
  private Integer price;               // 가격(원)
  private Integer remainingQuantity;   // 남은 수량
}
