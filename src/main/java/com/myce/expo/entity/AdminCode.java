package com.myce.expo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "expo_admin_code")
@EntityListeners(AuditingEntityListener.class)
public class AdminCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expo_admin_code_id")
    private Long id;

    @Setter
    @OneToOne(mappedBy = "adminCode")
    private AdminPermission adminPermission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Column(name = "expired_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Builder
    public AdminCode(Expo expo, String code, LocalDateTime expiredAt) {
        this.expo = expo;
        this.code = code;
        this.expiredAt = expiredAt;
    }

}
