package com.myce.payment.repository;

import com.myce.common.entity.type.TargetType;
import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import com.myce.payment.entity.type.PaymentTargetType;
import com.myce.payment.entity.type.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByPayment(Payment payment);
    // 특정 결제에 대한 상태별 환불 조회
    Optional<Refund> findByPaymentAndStatus(Payment payment, RefundStatus status);

    // 특정 결제에 대한 대기 중인 환불 신청이 있는지 확인
    boolean existsByPaymentAndStatus(Payment payment, RefundStatus status);
    
    // 플랫폼 관리자용: 모든 환불 신청 목록 조회
    Page<Refund> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 플랫폼 관리자용: 상태별 환불 신청 목록 조회
    Page<Refund> findByStatusOrderByCreatedAtDesc(RefundStatus status, Pageable pageable);

    void deleteByPayment(Payment payment);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.status = 'REFUNDED' " +
            "AND r.payment.targetType = :targetType " +
            "AND r.refundedAt BETWEEN :refundedAtAfter AND :refundedAtBefore")
    Long sumRefundAmountByTypeAndRefundedAtBetween(PaymentTargetType targetType,
                                                   LocalDateTime refundedAtAfter, LocalDateTime refundedAtBefore);

}
