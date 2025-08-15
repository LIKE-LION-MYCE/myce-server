package com.myce.payment.entity;

import com.myce.payment.entity.type.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "refund")
@EntityListeners(AuditingEntityListener.class)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "refund_amount", nullable = false)
    private Integer amount;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50)")
    private RefundStatus status;

    @Column(name = "is_partial", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isPartial;

    @Column(name = "refunded_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime refundedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Builder
    public Refund(Payment payment, Integer amount, String reason,
                  RefundStatus status, Boolean isPartial, LocalDateTime refundedAt) {
        this.payment = payment;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.isPartial = isPartial;
        this.refundedAt = refundedAt;
    }

    public void updateToRefund() {
        this.status = RefundStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }
}
