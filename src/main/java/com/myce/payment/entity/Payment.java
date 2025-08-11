package com.myce.payment.entity;

import com.myce.payment.entity.type.PaymentMethod;
import com.myce.payment.entity.type.PaymentTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "Payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UniqueTargetTypeTargetId",
                        columnNames = {"target_type", "target_id"})
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private PaymentTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, columnDefinition = "VARCHAR(50)")
    private PaymentMethod paymentMethod;

    @Column(name = "provider", length = 50, nullable = false)
    private String provider;

    @Column(name = "merchant_uid", length = 100, nullable = false)
    private String merchantUid;

    @Column(name = "imp_uid", length = 100, nullable = false)
    private String impUid;

    @Column(name = "card_company", length = 100)
    private String cardCompany;

    @Column(name = "card_number", length = 200)
    private String cardNumber;

    @Column(name = "account_bank", length = 100)
    private String accountBank;

    @Column(name = "account_number", length = 200)
    private String accountNumber;

    @Column(name = "country", length = 50)
    private String country;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "paid_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime paidAt;

    public void updateOnSuccess(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}