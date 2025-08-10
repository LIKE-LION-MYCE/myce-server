package com.myce.qrcode.repository;

import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    Optional<QrCode> findByQrToken(String token);
    Optional<QrCode> findByReserver(Reserver reserver);
    Optional<QrCode> findByReserverId(Long reserverId);
    
    // 스케줄링용 메서드들
    List<QrCode> findByStatusAndActivatedAtBefore(QrCodeStatus status, LocalDateTime activatedAt);
    List<QrCode> findByStatusAndExpiredAtBefore(QrCodeStatus status, LocalDateTime expiredAt);

}