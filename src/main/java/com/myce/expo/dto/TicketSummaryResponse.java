package com.myce.expo.dto;

import com.myce.expo.entity.type.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class TicketSummaryResponse {
  private Long ticketId;               // 식별자
  private String name;                 // 티켓명
  private TicketType type;             // 티켓 타입
  private Integer price;               // 가격(원)
  private Integer remainingQuantity;   // 남은 수량
  private LocalDate saleStartDate;     // 판매 시작일
  private LocalDate saleEndDate;       // 판매 종료일
  private String description;          // 티켓 설명
}
