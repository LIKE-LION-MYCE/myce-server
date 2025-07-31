package com.myce.system.entity;

import com.myce.system.entity.code.BillingCode;
import com.myce.system.entity.code.BillingTargetType;
import com.myce.system.entity.code.BillingUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_setting_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 10, nullable = false)
    private BillingTargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", length = 50, nullable = false)
    private BillingCode code;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "billing_message", length = 100)
    private String billingMessage;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", length = 20, nullable = false)
    private BillingUnit unit;

    @Column(name = "active")
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
