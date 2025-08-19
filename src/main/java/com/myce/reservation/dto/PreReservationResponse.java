package com.myce.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreReservationResponse {
  private Long reservationId;
  private String sessionId; // Redis 세션 ID 추가
  
  // 기존 생성자 유지
  public PreReservationResponse(Long reservationId) {
    this.reservationId = reservationId;
  }
}
