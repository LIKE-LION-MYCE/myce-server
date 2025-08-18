package com.myce.system.entity;

import com.myce.system.entity.type.StandardType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "refund_fee_setting")
public class RefundFeeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_fee_setting_id")
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard_type", nullable = false, columnDefinition = "VARCHAR(30)")
    private StandardType standardType;

    @Column(name = "standard_day_count", nullable = false)
    private int standardDayCount;

    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate;

    @Column(name="is_active", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isActive;

    @Column(name="valid_from", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime validFrom;

    @Column(name="valid_until", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime validUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public RefundFeeSetting(String name, StandardType standardType, int standardDayCount,
            BigDecimal feeRate, String description, LocalDateTime validFrom, LocalDateTime validUntil) {
        this.name = name;
        this.standardType = standardType;
        this.standardDayCount = standardDayCount;
        this.feeRate = feeRate;
        this.description = description;
        this.isActive = checkIsActive(validFrom, validUntil);
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public void updateFeeSetting(String name, int standardDayCount,
            BigDecimal feeRate, String description, LocalDateTime validFrom, LocalDateTime validUntil) {
        this.name = name;
        this.standardDayCount = standardDayCount;
        this.feeRate = feeRate;
        this.description = description;
//        this.isActive = checkIsActive(validFrom, validUntil);
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    private boolean checkIsActive(LocalDateTime validFrom, LocalDateTime validUntil) {
        LocalDateTime now = LocalDateTime.now();
        return validFrom.isBefore(now) && validUntil.isAfter(now);
    }

    public void inactive() {
        this.isActive = false;
    }

    public void active() {
        this.isActive = true;
    }
}
