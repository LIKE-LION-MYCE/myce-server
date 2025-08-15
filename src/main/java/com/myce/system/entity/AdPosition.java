package com.myce.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "ad_position")
@EntityListeners(AuditingEntityListener.class)
public class AdPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_position_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(50)")
    private String name;

    @Column(name = "image_width", nullable = false)
    private Integer imageWidth;

    @Column(name = "image_height", nullable = false)
    private Integer imageHeight;

    @Column(name = "max_count", nullable = false)
    private Integer maxCount;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public AdPosition(String name, Integer imageWidth, Integer imageHeight,
                     Integer maxCount, Boolean isActive) {
        this.name = name;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.maxCount = maxCount;
        this.isActive = isActive;
    }

    public void update(String name, Integer imageWidth,
                       Integer imageHeight, Integer maxCount,
                       boolean isActive, LocalDateTime updatedAt) {
        this.name = name;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.maxCount = maxCount;
        this.isActive = isActive;
    }
}