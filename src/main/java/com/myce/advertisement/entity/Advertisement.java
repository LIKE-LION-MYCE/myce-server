package com.myce.advertisement.entity;

import com.myce.member.entity.Member;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "advertisement")
@EntityListeners(AuditingEntityListener.class)
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advertisement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    @Column(name = "link_url", length = 500, nullable = false)
    private String linkUrl;

    @Column(name = "exposure_position", length = 50, nullable = false)
    private String exposurePosition;

    @Column(name = "display_start_date", nullable = false)
    private LocalDate displayStartDate;

    @Column(name = "display_end_date", nullable = false)
    private LocalDate displayEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50)")
    private AdvertisementStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Advertisement(Member member, String title, String description, String imageUrl,
                         String linkUrl, String exposurePosition, LocalDate displayStartDate,
                         LocalDate displayEndDate, AdvertisementStatus status) {
        this.member = member;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.exposurePosition = exposurePosition;
        this.displayStartDate = displayStartDate;
        this.displayEndDate = displayEndDate;
        this.status = status;
    }
}
