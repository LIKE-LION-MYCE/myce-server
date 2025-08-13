package com.myce.reservation.service;

public interface ReservationCodeService {
  // 생성
  /**
   * 포맷: RES{expoId(3자리)}-YYYYMMDD-{랜덤 6자리 Base36}
   * 예) RES001-20240801-7K3F9X
   */
  String generate(Long expoId);
}
