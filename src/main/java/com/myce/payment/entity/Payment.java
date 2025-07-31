package com.myce.payment.entity;

import com.myce.member.entity.Member;
import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.entity.type.PaymentTargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "payment")
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "VARCHAR(20)")
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
    @Column(name = "payment_method", nullable = false, columnDefinition = "VARCHAR(50)")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50)")
    private PaymentStatus status;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "merchant_uid", length = 100)
    private String merchantUid;

    @Column(name = "imp_uid", length = 100)
    private String impUid;

    @Column(name = "card_company", length = 100)
    private String cardCompany;

    @Column(name = "card_number", length = 200)
    private String cardNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 200)
    private String accountNumber;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "paid_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime refundedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;



}
