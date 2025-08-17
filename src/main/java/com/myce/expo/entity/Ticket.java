package com.myce.expo.entity;

import com.myce.expo.entity.type.TicketType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Entity
@NoArgsConstructor
@Table(name = "ticket")
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "VARCHAR(20)")
    private TicketType type;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "sale_start_date", nullable = false)
    private LocalDate saleStartDate;

    @Column(name = "sale_end_date", nullable = false)
    private LocalDate saleEndDate;

    @Column(name = "use_start_date", nullable = false)
    private LocalDate useStartDate;

    @Column(name = "use_end_date", nullable = false)
    private LocalDate useEndDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Ticket(Expo expo, String name, String description, TicketType type,
                  Integer price, Integer remainingQuantity, Integer totalQuantity,
                  LocalDate useStartDate, LocalDate useEndDate,
                  LocalDate saleStartDate, LocalDate saleEndDate) {
        this.expo = expo;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.remainingQuantity = remainingQuantity;
        this.totalQuantity = totalQuantity;
        this.useStartDate = useStartDate;
        this.useEndDate = useEndDate;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
    }

    public void updateTicketInfo(String name, String description, TicketType type,
                                 Integer price, Integer remainingQuantity, Integer totalQuantity,
                                 LocalDate saleStartDate, LocalDate saleEndDate,
                                 LocalDate useStartDate, LocalDate useEndDate) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.remainingQuantity = remainingQuantity;
        this.totalQuantity = totalQuantity;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
        this.useStartDate = useStartDate;
        this.useEndDate = useEndDate;
    }

    /**
     * 티켓 재고 복구 (환불로 인한 판매 취소 시 사용)
     * 박람회 취소 시 판매된 티켓 수량만큼 재고를 복구합니다.
     * 
     * @param quantity 복구할 수량
     */
    public void restoreQuantity(Integer quantity) {
        this.remainingQuantity += quantity;
        // 안전장치: 총 수량을 초과하지 않도록 제한
        if (this.remainingQuantity > this.totalQuantity) {
            this.remainingQuantity = this.totalQuantity;
        }
    }

    public void updateRemainingQuantity(Integer quantity){
        this.remainingQuantity = quantity;
    }

}