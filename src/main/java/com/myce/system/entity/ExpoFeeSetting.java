package com.myce.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "expo_fee_setting")
@EntityListeners(AuditingEntityListener.class)
public class ExpoFeeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expo_fee_setting_id")
    private Long id;

    @Column(name="name", nullable = false, length = 30)
    private String name;

    @Column(name = "deposit", nullable = false)
    private Integer deposit;

    @Column(name = "premium_deposit", nullable = false)
    private Integer premiumDeposit;

    @Column(name = "settlement_commission", nullable = false, precision = 5, scale = 2, columnDefinition = "DECIMAL(5, 2)")
    private BigDecimal settlementCommission;

    @Column(name = "daily_usage_fee", nullable = false)
    private Integer dailyUsageFee;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public ExpoFeeSetting(String name, Integer deposit, Integer premiumDeposit, BigDecimal settlementCommission,
                          Integer dailyUsageFee, Boolean isActive) {
        this.name = name;
        this.deposit = deposit;
        this.premiumDeposit = premiumDeposit;
        this.settlementCommission = settlementCommission;
        this.dailyUsageFee = dailyUsageFee;
        this.isActive = isActive;
    }

    public void inactive() {
        this.isActive = false;
    }

    public void active() {
        this.isActive = true;
    }
}