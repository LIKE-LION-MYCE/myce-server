package com.myce.qrcode.entity;

import com.myce.qrcode.entity.code.QrCodeStatus;
import com.myce.reservation.entity.Reserver;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qr_code_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserver_id")
    private Reserver reserver;

    @Column(name = "qr_token", length = 255)
    private String qrToken;

    @Column(name = "qr_image_url", length = 255)
    private String qrImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private QrCodeStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
