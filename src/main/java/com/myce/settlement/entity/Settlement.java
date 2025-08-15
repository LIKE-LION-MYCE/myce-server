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

    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "supply_amount")
    private Integer supplyAmount;

    @Column(name = "settle_amount")
    private Integer settleAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, columnDefinition = "VARCHAR(50)")
    private SettlementStatus settlementStatus;

    @Column(name = "settlement_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime settlementAt;

    @Column(name = "receiver_name", nullable = true, length = 100)
    private String receiverName;

    @Column(name = "bank_name", nullable = true, length = 10)
    private String bankName;

    @Column(name = "bank_account", nullable = true, length = 200)
    private String bankAccount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    public Settlement(Expo expo, Member adminMember, Integer totalAmount, Integer supplyAmount, Integer settleAmount,
                      SettlementStatus settlementStatus, LocalDateTime settlementAt, String receiverName,
                      String bankName, String bankAccount) {
        this.expo = expo;
        this.adminMember = adminMember;
        this.totalAmount = totalAmount;
        this.supplyAmount = supplyAmount;
        this.settleAmount = settleAmount;
        this.settlementStatus = settlementStatus;
        this.settlementAt = settlementAt;
        this.receiverName = receiverName;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }
    
    /**
     * 은행 정보 업데이트 및 정산 요청 상태로 변경
     * 박람회 관리자가 정산 신청할 때 사용
     * 
     * @param receiverName 예금주명
     * @param bankName 은행명
     * @param bankAccount 계좌번호
     */
    public void updateBankInfo(String receiverName, String bankName, String bankAccount) {
        this.receiverName = receiverName;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.settlementStatus = SettlementStatus.SETTLEMENT_REQUESTED;
    }
}
