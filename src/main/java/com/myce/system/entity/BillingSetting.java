package com.myce.system.entity;

import com.myce.system.entity.type.BillingCode;
import com.myce.system.entity.type.BillingTargetType;
import com.myce.system.entity.type.BillingUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "billing_setting")
public class BillingSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_setting_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private BillingTargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, columnDefinition = "VARCHAR(50)")
    private BillingCode code;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "billing_message", length = 100, nullable = false)
    private String billingMessage;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, columnDefinition = "VARCHAR(10)")
    private BillingUnit unit;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public BillingSetting(BillingTargetType targetType, BillingCode code,
                          String name, String billingMessage,
                          BigDecimal amount, BillingUnit unit, Boolean isActive) {
        this.targetType = targetType;
        this.code = code;
        this.name = name;
        this.billingMessage = billingMessage;
        this.amount = amount;
        this.unit = unit;
        this.isActive = isActive;
    }
}
