package com.myce.reservation.repository;

import com.myce.reservation.dto.ExpoAdminReservationResponse;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.Reserver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReserverRepository extends JpaRepository<Reserver, Long> {
    
    List<Reserver> findByReservation(Reservation reservation);
    
    // QR코드 일괄 생성용 - 특정 박람회의 모든 예약자 조회
    @Query("SELECT r FROM Reserver r " +
           "JOIN r.reservation res " +
           "WHERE res.expo.id = :expoId ")
    List<Reserver> findReserversByExpo(@Param("expoId") Long expoId);

    @Query("""
        SELECT NEW com.myce.reservation.dto.ExpoAdminReservationResponse(
          rv.id,
          r.reservationCode,
          rv.name,
          CASE
            WHEN rv.gender = com.myce.member.entity.type.Gender.FEMALE THEN '여'
            WHEN rv.gender = com.myce.member.entity.type.Gender.MALE THEN '남'
            ELSE '-'
          END,
          rv.phone,
          rv.email,
          t.name,
          qc.usedAt,
          CASE
            WHEN qc.status = com.myce.qrcode.entity.code.QrCodeStatus.USED THEN '입장 완료'
            WHEN qc.status = com.myce.qrcode.entity.code.QrCodeStatus.EXPIRED THEN '티켓 만료'
            WHEN qc.id IS NULL OR qc.status = com.myce.qrcode.entity.code.QrCodeStatus.ACTIVE THEN '입장 전'
            ELSE '입장 전'
          END
        )
        FROM Reserver rv
        JOIN rv.reservation r
        JOIN r.ticket t
        LEFT JOIN com.myce.qrcode.entity.QrCode qc ON qc.reserver = rv
        WHERE r.expo.id = :expoId
          AND r.status = com.myce.reservation.entity.code.ReservationStatus.CONFIRMED
          AND (:name IS NULL OR LOWER(rv.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:phone IS NULL OR rv.phone LIKE CONCAT('%', :phone, '%'))
          AND (:reservationCode IS NULL OR r.reservationCode LIKE CONCAT('%', :reservationCode, '%'))
          AND (:ticketName IS NULL OR t.name = :ticketName)
          AND (
            :entranceStatus IS NULL OR
            (
              (:entranceStatus = '입장 완료' AND qc.status = com.myce.qrcode.entity.code.QrCodeStatus.USED)
              OR (:entranceStatus = '입장 전' AND (qc.id IS NULL
                  OR qc.status = com.myce.qrcode.entity.code.QrCodeStatus.ACTIVE))
              OR (:entranceStatus = '티켓 만료' AND qc.status = com.myce.qrcode.entity.code.QrCodeStatus.EXPIRED)
            )
          )
    """)
    Page<ExpoAdminReservationResponse> findAllResponsesByExpoIdAndStatus(
            @Param("expoId") Long expoId,
            @Param("entranceStatus") String entranceStatus,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("reservationCode") String reservationCode,
            @Param("ticketName") String ticketName,
            Pageable pageable
    );
}