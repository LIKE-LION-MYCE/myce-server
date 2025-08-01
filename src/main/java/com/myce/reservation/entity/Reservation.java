package com.myce.reservation.entity;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.member.entity.Member;
import com.myce.reservation.entity.code.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "reservation")
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "reservation_code", length = 30, nullable = false)
    private String reservationCode;

    @Column(name = "is_member", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isMember;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(30)")
    private ReservationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Reservation(Expo expo, Ticket ticket, Member member, String reservationCode,
                       Boolean isMember, Integer quantity, ReservationStatus status) {
        this.expo = expo;
        this.ticket = ticket;
        this.member = member;
        this.reservationCode = reservationCode;
        this.isMember = isMember;
        this.quantity = quantity;
        this.status = status;
    }
}
