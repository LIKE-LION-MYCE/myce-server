package com.myce.payment.entity;

import com.myce.member.entity.Member;
import com.myce.payment.entity.type.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "reservation_payment_info")
@EntityListeners(AuditingEntityListener.class)
public class ReservationPaymentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_payment_info_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "used_mileage", nullable = false)
    private Integer usedMileage;

    @Column(name = "saved_mileage", nullable = false)
    private Integer savedMileage;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(30)")
    private PaymentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false,  columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public ReservationPaymentInfo(Member member, Integer usedMileage, Integer savedMileage,
                                  Integer totalAmount, PaymentStatus status) {
        this.member = member;
        this.usedMileage = usedMileage;
        this.savedMileage = savedMileage;
        this.totalAmount = totalAmount;
        this.status = status;
    }
}