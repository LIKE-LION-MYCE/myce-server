package com.myce.reservation.entity;

import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
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

    @Column(name = "reservation_code", length = 30, nullable = false)
    private String reservationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, columnDefinition = "VARCHAR(10)")
    private UserType userType;

    @Column(name = "user_id", nullable = false)
    private Long userId;

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
    public Reservation(Expo expo, Ticket ticket, String reservationCode,
            UserType userType, Long userId, Integer quantity, ReservationStatus status) {
        this.expo = expo;
        this.ticket = ticket;
        this.reservationCode = reservationCode;
        this.userType = userType;
        this.userId = userId;
        this.quantity = quantity;
        this.status = status;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    public void updateGuestId(Long guestId) {
        this.userId = guestId;
    }
}
