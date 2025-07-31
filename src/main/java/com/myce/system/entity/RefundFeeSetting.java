package com.myce.system.entity;

import com.myce.reservation.entity.Reservation;
import com.myce.system.entity.code.StandardType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "refund_fee_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundFeeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_fee_setting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(name = "start_day", nullable = false)
    private LocalDate startDay;

    @Column(name = "end_day", nullable = false)
    private LocalDate endDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "standard_type", nullable = false)
    private StandardType standardType;

    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate;

    @Column(name = "description", length = 255)
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDate updatedAt;

}
