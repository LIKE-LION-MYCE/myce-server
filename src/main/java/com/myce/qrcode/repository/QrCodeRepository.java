package com.myce.qrcode.repository;

import com.myce.qrcode.entity.QrCode;
import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    List<QrCode> findByReserverIdIn(List<Long> reserverIds);

    // === 대시보드 통계용 쿼리 메서드들 ===

    // 특정 박람회의 QR 체크인 성공 건수
    @Query("SELECT COUNT(qr) FROM QrCode qr " +
            "JOIN qr.reserver rv " +
            "JOIN rv.reservation r " +
            "WHERE r.expo.id = :expoId " +
            "AND qr.status = 'USED'")
    Long countSuccessfulCheckinsByExpoId(@Param("expoId") Long expoId);

    // 특정 박람회의 오늘 시간대별 입장인원
    @Query("SELECT HOUR(qr.usedAt) as hour, COUNT(qr) as count " +
            "FROM QrCode qr " +
            "JOIN qr.reserver rv " +
            "JOIN rv.reservation r " +
            "WHERE r.expo.id = :expoId " +
            "AND qr.status = 'USED' " +
            "AND DATE(qr.usedAt) = :today " +
            "GROUP BY HOUR(qr.usedAt) " +
            "ORDER BY HOUR(qr.usedAt)")
    List<Object[]> countHourlyCheckinsByExpoIdAndDate(@Param("expoId") Long expoId, @Param("today") LocalDate today);
    
    // 특정 사용자가 특정 박람회에 참석했는지 확인 (QR 코드가 USED 상태)
    @Query("SELECT COUNT(qr) > 0 FROM QrCode qr " +
            "JOIN qr.reserver rv " +
            "JOIN rv.reservation r " +
            "WHERE r.expo.id = :expoId " +
            "AND r.userId = :memberId " +
            "AND r.userType = 'MEMBER' " +
            "AND qr.status = 'USED'")
    boolean existsByExpoIdAndMemberIdAndStatusUsed(@Param("expoId") Long expoId, @Param("memberId") Long memberId);
}