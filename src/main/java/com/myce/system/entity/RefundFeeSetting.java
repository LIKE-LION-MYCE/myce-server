package com.myce.system.entity;

import com.myce.system.entity.type.StandardType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "refund_fee_setting")
public class RefundFeeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_fee_setting_id")
    private Long id;

    @Column(name = "start_day", nullable = false)
    private LocalDate startDay;

    @Column(name = "end_day", nullable = false)
    private LocalDate endDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard_type", nullable = false, columnDefinition = "VARCHAR(30)")
    private StandardType standardType;

    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public RefundFeeSetting(LocalDate startDay, LocalDate endDay,
                            StandardType standardType, BigDecimal feeRate, String description) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.standardType = standardType;
        this.feeRate = feeRate;
        this.description = description;
    }
}
