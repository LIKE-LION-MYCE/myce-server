package com.myce.expo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor
@Table(name = "admin_code")
@EntityListeners(AuditingEntityListener.class)
public class AdminCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_code_id")
    private Long id;

    @Column(name = "expo_id", nullable = false)
    private Long expoId;

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Column(name = "issued_by", length = 20, nullable = false)
    private String issuedBy;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "adminCode")
    private List<Permission> permissions;

    @Builder
    public AdminCode(Long expoId, String code, String issuedBy, LocalDateTime expiredAt) {
        this.expoId = expoId;
        this.code = code;
        this.issuedBy = issuedBy;
        this.expiredAt = expiredAt;
        this.permissions = new ArrayList<>();
    }

    public void addPermission(Permission permission) {
        permission.setAdminCode(this);
        this.permissions.add(permission);
    }
}
