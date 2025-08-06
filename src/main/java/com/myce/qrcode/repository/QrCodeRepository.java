package com.myce.qrcode.repository;

import com.myce.qrcode.entity.QrCode;
import com.myce.reservation.entity.Reserver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    Optional<QrCode> findByQrToken(String token);
    Optional<QrCode> findByReserver(Reserver reserver);

}