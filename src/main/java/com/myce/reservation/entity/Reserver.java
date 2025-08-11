package com.myce.reservation.entity;

import com.myce.member.entity.type.Gender;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "reserver")
@EntityListeners(AuditingEntityListener.class)
public class Reserver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserver_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, columnDefinition = "VARCHAR(6)")
    private Gender gender;

    @Column(name = "phone", length = 13, nullable = false)
    private String phone;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Reserver(Reservation reservation, String name,
                    Gender gender, String phone, String email, LocalDate birth) {
        this.reservation = reservation;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.birth = birth;
    }

    public void updateReserverInfo(String name, Gender gender, String phone, String email) {
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
    }
}