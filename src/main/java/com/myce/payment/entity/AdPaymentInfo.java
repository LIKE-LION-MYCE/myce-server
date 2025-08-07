package com.myce.payment.entity;

import com.myce.advertisement.entity.Advertisement;
import com.myce.payment.entity.type.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "ad_payment_info")
@EntityListeners(AuditingEntityListener.class)
public class AdPaymentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_payment_info_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

    @Column(name = "total_day", nullable = false)
    private Integer totalDay;

    @Column(name = "fee_per_day", nullable = false)
    private Integer feePerDay;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(100)")
    private PaymentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public AdPaymentInfo(Advertisement advertisement, Integer totalDay, Integer feePerDay,
                         Integer totalAmount, PaymentStatus status) {
        this.advertisement = advertisement;
        this.totalDay = totalDay;
        this.feePerDay = feePerDay;
        this.totalAmount = totalAmount;
        this.status = status;
    }
}