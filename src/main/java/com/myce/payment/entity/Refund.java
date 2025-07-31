package com.myce.payment.entity;

import com.myce.payment.entity.code.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    @Column(name = "refund_reason", length = 255)
    private String refundReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private RefundStatus status;

    @Column(name = "is_partial")
    private Boolean isPartial;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
