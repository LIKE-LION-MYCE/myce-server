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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Ticket(Expo expo, String name, String description, TicketType type,
                  Integer price, Integer remainingQuantity, Integer totalQuantity,
                  LocalDate saleStartDate, LocalDate saleEndDate) {
        this.expo = expo;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.remainingQuantity = remainingQuantity;
        this.totalQuantity = totalQuantity;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
    }

    public void updateTicketInfo(String name, String description, TicketType type,
                                 Integer price, Integer remainingQuantity, Integer totalQuantity,
                                 LocalDate saleStartDate, LocalDate saleEndDate) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.remainingQuantity = remainingQuantity;
        this.totalQuantity = totalQuantity;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
    }
}
