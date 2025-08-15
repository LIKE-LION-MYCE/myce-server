package com.myce.payment.entity;

import com.myce.expo.entity.Expo;
import com.myce.payment.entity.type.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "expo_payment_info")
@EntityListeners(AuditingEntityListener.class)
public class ExpoPaymentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expo_payment_info_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(100)")
    private PaymentStatus status;

    @Column(name = "deposit", nullable = false)
    private Integer deposit;

    @Column(name = "premium_deposit", nullable = false)
    private Integer premiumDeposit;

    @Column(name = "total_day", nullable = false)
    private Integer totalDay;

    @Column(name = "daily_usage_fee", nullable = false)
    private Integer dailyUsageFee;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal commissionRate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public ExpoPaymentInfo(Expo expo, PaymentStatus status, Integer deposit, Integer premiumDeposit,
            Integer totalDay, Integer dailyUsageFee, Integer totalAmount, BigDecimal commissionRate) {
        this.expo = expo;
        this.status = status;
        this.deposit = deposit;
        this.premiumDeposit = premiumDeposit;
        this.totalDay = totalDay;
        this.dailyUsageFee = dailyUsageFee;
        this.totalAmount = totalAmount;
        this.commissionRate = commissionRate;
    }

    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }
}