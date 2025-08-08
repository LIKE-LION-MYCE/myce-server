package com.myce.expo.entity;

import com.myce.expo.dto.MyExpoUpdateRequest;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.member.entity.Member;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "expo")
@EntityListeners(AuditingEntityListener.class)
public class Expo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "expo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpoCategory> expoCategories = new ArrayList<>();

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "thumbnail_url", length = 500, nullable = false)
    private String thumbnailUrl;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "location", length = 100, nullable = false)
    private String location;

    @Column(name = "location_detail", length = 100, nullable = false)
    private String locationDetail;

    @Column(name = "max_reserver_count", nullable = false)
    private Integer maxReserverCount;

    @Column(name = "latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal longitude;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50)")
    private ExpoStatus status;

    @Column(name = "display_start_date", nullable = false)
    private LocalDate displayStartDate;

    @Column(name = "display_end_date", nullable = false)
    private LocalDate displayEndDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "start_time", nullable = false, columnDefinition = "TIME")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false, columnDefinition = "TIME")
    private LocalTime endTime;

    @Column(name = "is_premium", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isPremium;

    @Builder
    public Expo(Member member, String title, String thumbnailUrl, String description,
                String location, String locationDetail, Integer maxReserverCount,
                BigDecimal latitude, BigDecimal longitude, LocalDate startDate,
                LocalDate endDate, ExpoStatus status, LocalDate displayStartDate,
                LocalDate displayEndDate, LocalTime startTime, LocalTime endTime, Boolean isPremium) {
        this.member = member;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.location = location;
        this.locationDetail = locationDetail;
        this.maxReserverCount = maxReserverCount;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.displayStartDate = displayStartDate;
        this.displayEndDate = displayEndDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isPremium = isPremium;
    }

    // DTO를 사용하여 엔티티의 필드를 업데이트하는 메서드 (더티 체킹 활용)
    public void updateFromDto(MyExpoUpdateRequest dto) {
        this.title = dto.getTitle();
        this.thumbnailUrl = dto.getThumbnailUrl();
        this.description = dto.getDescription();
        this.location = dto.getLocation();
        this.locationDetail = dto.getLocationDetail();
        this.maxReserverCount = dto.getMaxReserverCount();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.displayStartDate = dto.getDisplayStartDate();
        this.displayEndDate = dto.getDisplayEndDate();
        this.startTime = dto.getStartTime();
        this.endTime = dto.getEndTime();
        this.isPremium = dto.getIsPremium();
    }
    
    public void cancel() {
        if (this.status != ExpoStatus.PENDING_APPROVAL && 
            this.status != ExpoStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("취소할 수 없는 박람회 상태입니다: " + this.status);
        }
        this.status = ExpoStatus.CANCELLED;
    }
}
