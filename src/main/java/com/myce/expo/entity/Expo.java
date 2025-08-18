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

    // 설명만 업데이트하는 메서드 (PENDING_PUBLISH 상태에서 부분 수정용)
    public void updateDescription(String description) {
        this.description = description;
    }
    
    public void cancel() {
        if (this.status != ExpoStatus.PENDING_APPROVAL && 
            this.status != ExpoStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("취소할 수 없는 박람회 상태입니다: " + this.status);
        }
        this.status = ExpoStatus.CANCELLED;
    }
    
    /**
     * 박람회 승인 처리
     * PENDING_APPROVAL -> PENDING_PAYMENT
     */
    public void approve() {
        if (this.status != ExpoStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("승인 대기 상태의 박람회만 승인할 수 있습니다: " + this.status);
        }
        this.status = ExpoStatus.PENDING_PAYMENT;
    }
    
    /**
     * 박람회 거절 처리
     * PENDING_APPROVAL -> REJECTED
     */
    public void reject() {
        if (this.status != ExpoStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("승인 대기 상태의 박람회만 거절할 수 있습니다: " + this.status);
        }
        this.status = ExpoStatus.REJECTED;
    }
    
    /**
     * 박람회 취소 승인 처리
     * PENDING_CANCEL -> CANCELLED
     */
    public void approveCancellation() {
        if (this.status != ExpoStatus.PENDING_CANCEL) {
            throw new IllegalStateException("취소 대기 상태의 박람회만 취소 승인할 수 있습니다: " + this.status);
        }
        this.status = ExpoStatus.CANCELLED;
    }
    
    /**
     * 박람회 자동 게시 처리 (스케줄러용)
     * PENDING_PUBLISH -> PUBLISHED
     */
    public void publish() {
        if (this.status != ExpoStatus.PENDING_PUBLISH) {
            throw new IllegalStateException("게시 대기 상태의 박람회만 게시할 수 있습니다: " + this.status);
        }
        this.status = ExpoStatus.PUBLISHED;
    }
    
    /**
     * 박람회 자동 게시 종료 처리 (스케줄러용)
     * PUBLISHED -> PUBLISH_ENDED
     */
    public void complete() {
        if (this.status != ExpoStatus.PUBLISHED) {
            throw new IllegalStateException("게시 중인 박람회만 종료할 수 있습니다: " + this.status);
        }
        this.status = ExpoStatus.PUBLISH_ENDED;
    }
    
    /**
     * 박람회 정산 승인 처리 (플랫폼 관리자용)
     * SETTLEMENT_REQUESTED -> COMPLETED
     */
    public void approveSettlement() {
        if (this.status != ExpoStatus.SETTLEMENT_REQUESTED) {
            throw new IllegalStateException("정산 요청 상태의 박람회만 정산 승인할 수 있습니다: " + this.status);
        }
        this.status = ExpoStatus.COMPLETED;
    }
    
    // 상태별 취소 처리
    public void cancelByStatus() {
        switch (this.status) {
            case PENDING_APPROVAL:
            case PENDING_PAYMENT:
                this.status = ExpoStatus.CANCELLED;
                break;
            default:
                throw new IllegalStateException("취소할 수 없는 박람회 상태입니다: " + this.status);
        }
    }
    
    // 상태 직접 변경 메서드
    public void updateStatus(ExpoStatus newStatus) {
        this.status = newStatus;
    }
}
