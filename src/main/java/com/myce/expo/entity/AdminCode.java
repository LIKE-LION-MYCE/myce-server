package com.myce.expo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_code")
@Getter
@Setter
@NoArgsConstructor
public class AdminCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_code_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Column(name = "issued_by", length = 20, nullable = false)
    private String issuedBy;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public AdminCode(Expo expo, String code, String issuedBy, LocalDateTime expiredAt) {
        this.expo = expo;
        this.code = code;
        this.issuedBy = issuedBy;
        this.expiredAt = expiredAt;
    }
}
