package com.myce.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreReservationResponse {
  private String preReservationId; // reservationCode를 저장
  
  // 레거시 호환성을 위한 생성자
  public PreReservationResponse(Long reservationId) {
    this.preReservationId = reservationId.toString();
  }
}
