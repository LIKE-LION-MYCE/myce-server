package com.myce.settlement.entity;

import com.myce.expo.entity.Expo;
import com.myce.member.entity.Member;
import com.myce.settlement.entity.code.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "settlement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_member_id")
    private Member adminMember;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "supply_amount", nullable = false)
    private Long supplyAmount;

    @Column(name = "settle_amount", nullable = false)
    private Long settleAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", length = 50, nullable = false)
    private SettlementStatus settlementStatus;

    @Column(name = "settlement_at")
    private LocalDateTime settlementAt;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "bank_name", length = 10)
    private String bankName;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
