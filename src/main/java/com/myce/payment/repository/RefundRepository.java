package com.myce.payment.repository;

import com.myce.payment.entity.Payment;
import com.myce.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByPayment(Payment payment);
}
