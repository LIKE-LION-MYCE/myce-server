package com.myce.expo.entity;

import com.myce.expo.entity.type.PermissionFeature;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter @Entity
@NoArgsConstructor
@Table(name = "permission")
@EntityListeners(AuditingEntityListener.class)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_code_id", nullable = false)
    private AdminCode adminCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature", length = 50, nullable = false)
    private PermissionFeature feature;

    @Column(name = "is_allowed", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isAllowed;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Permission(AdminCode adminCode, PermissionFeature feature, Boolean isAllowed) {
        this.adminCode = adminCode;
        this.feature = feature;
        this.isAllowed = isAllowed;
    }
}
