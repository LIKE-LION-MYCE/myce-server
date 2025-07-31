package com.myce.payment.entity;

import com.myce.member.entity.Member;
import com.myce.payment.entity.code.PaymentMethod;
import com.myce.payment.entity.code.PaymentStatus;
import com.myce.payment.entity.code.PaymentTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20, nullable = false)
    private PaymentTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "used_mileage")
    private Integer usedMileage;

    @Column(name = "saved_mileage")
    private Integer savedMileage;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50, nullable = false)
    private PaymentMethod paymentMethod;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PaymentStatus status;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "merchant_uid", length = 100)
    private String merchantUid;

    @Column(name = "imp_uid", length = 100)
    private String impUid;

    @Column(name = "card_company", length = 100)
    private String cardCompany;

    @Column(name = "card_number", length = 50)
    private String cardNumber;

    @Column(name = "country", length = 10)
    private String country;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
}
