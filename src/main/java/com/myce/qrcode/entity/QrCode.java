package com.myce.qrcode.entity;

import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "qr_code")
@EntityListeners(AuditingEntityListener.class)
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qr_code_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserver_id", nullable = false)
    private Reserver reserver;

    @Column(name = "qr_token", length = 500, nullable = false)
    private String qrToken;

    @Column(name = "qr_image_url", length = 500, nullable = false)
    private String qrImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20)")
    private QrCodeStatus status;

    @Column(name = "activated_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime activatedAt;

    @Column(name = "expired_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime expiredAt;

    @Column(name = "used_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Builder
    public QrCode(Reserver reserver, String qrToken,
                  String qrImageUrl, QrCodeStatus status, LocalDateTime activatedAt
                  ,LocalDateTime expiredAt, LocalDateTime usedAt) {
        this.reserver = reserver;
        this.qrToken = qrToken;
        this.qrImageUrl = qrImageUrl;
        this.status = status;
        this.activatedAt = activatedAt;
        this.expiredAt = expiredAt;
        this.usedAt = usedAt;
    }

    public void markAsUsed() {
        this.status = QrCodeStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = QrCodeStatus.EXPIRED;
    }

    public void activate() { this. status = QrCodeStatus.ACTIVE; }

}
