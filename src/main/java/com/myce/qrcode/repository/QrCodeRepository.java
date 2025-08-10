package com.myce.qrcode.repository;

import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // 혼잡도 계산용 - 특정 박람회의 1시간 내 입장자 수 조회
    long countByReserverReservationExpoIdAndUsedAtAfter(Long expoId, LocalDateTime oneHourAgo);

    
    // QR코드 상태 일괄 업데이트 - APPROVED -> ACTIVE
    @Modifying(clearAutomatically = true)
    @Query("UPDATE QrCode qr SET qr.status = :newStatus " +
           "WHERE qr.status = :currentStatus " +
           "AND qr.activatedAt <= :currentTime")
    int bulkUpdateStatusToActive(@Param("currentStatus") QrCodeStatus currentStatus,
                                @Param("newStatus") QrCodeStatus newStatus,
                                @Param("currentTime") LocalDateTime currentTime);
    
    // QR코드 상태 일괄 업데이트 - ACTIVE -> EXPIRED
    @Modifying(clearAutomatically = true)
    @Query("UPDATE QrCode qr SET qr.status = :newStatus " +
           "WHERE qr.status = :currentStatus " +
           "AND qr.expiredAt <= :currentTime")
    int bulkUpdateStatusToExpired(@Param("currentStatus") QrCodeStatus currentStatus,
                                 @Param("newStatus") QrCodeStatus newStatus,
                                 @Param("currentTime") LocalDateTime currentTime);

}