package com.myce.common.entity;

import com.myce.common.entity.type.TargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(
        name = "reject_info",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UniqueTargetTypeTargetId",
                        columnNames = {"target_type", "target_id"})
        }
)
@EntityListeners(AuditingEntityListener.class)
public class RejectInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reject_info_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", columnDefinition = "VARCHAR(20)", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Builder
    public RejectInfo(TargetType targetType, Long targetId, String description) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
    }
}