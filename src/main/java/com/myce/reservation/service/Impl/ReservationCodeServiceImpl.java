package com.myce.reservation.service.Impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.service.ReservationCodeService;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationCodeServiceImpl implements ReservationCodeService {

  private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final char[] ALPHANUM_BASE36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  private static final int RANDOM_LEN = 6; // 충돌 확률/가독성 균형

  private final ReservationRepository reservationRepository;
  private final SecureRandom random = new SecureRandom();

  @Override
  @Transactional
  public String generate(Long expoId) {
    if (expoId == null) throw new CustomException(CustomErrorCode.RESERVATION_CODE_INVALID_INPUT);

    String datePart = LocalDate.now(ZONE_SEOUL).format(DATE_FMT);
    String prefix = "RES" + String.format("%03d", expoId) + "-" + datePart + "-";

    // 중복 방지를 위해 여러 번 시도
    for (int attempt = 0; attempt < 10; attempt++) {
      String candidate = prefix + randomBase36(RANDOM_LEN);
      if (!reservationRepository.existsByReservationCode(candidate)) {
        return candidate; // 현재 시점 기준 미사용 코드
      }
    }
    throw new CustomException(CustomErrorCode.RESERVATION_CODE_GENERATION_FAILED);
  }

  private String randomBase36(int len) {
    char[] buf = new char[len];
    for (int i = 0; i < len; i++) {
      buf[i] = ALPHANUM_BASE36[random.nextInt(ALPHANUM_BASE36.length)];
    }
    return new String(buf);
  }
}
