package com.myce.system.entity;

import com.myce.advertisement.entity.AdPosition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "ad_fee_setting",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ad_position_active",
                        columnNames = {"ad_position_id", "is_active"})
        })
@EntityListeners(AuditingEntityListener.class)
public class AdFeeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_fee_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_position_id", referencedColumnName = "ad_position_id", nullable = false)
    private AdPosition adPosition;

    @Column(name = "fee_per_day", nullable = false)
    private Integer feePerDay;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public AdFeeSetting(AdPosition adPosition, Integer feePerDay, Boolean isActive) {
        this.adPosition = adPosition;
        this.feePerDay = feePerDay;
        this.isActive = isActive;
    }
}