package com.myce.advertisement.entity;

import com.myce.advertisement.entity.type.AdvertisementStatus;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.member.entity.Member;
import com.myce.system.entity.AdPosition;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_position_id", referencedColumnName = "ad_position_id", nullable = false)
    private AdPosition adPosition;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

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
                         String linkUrl, AdPosition adPosition, Integer totalDays,
                         LocalDate displayStartDate, LocalDate displayEndDate, AdvertisementStatus status
                         ) {
        this.member = member;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.adPosition = adPosition;
        this.totalDays = totalDays;
        this.displayStartDate = displayStartDate;
        this.displayEndDate = displayEndDate;
        this.status = status;
    }

    public void approve() {
        if(this.status != AdvertisementStatus.PENDING_APPROVAL){
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.PENDING_PAYMENT;
    }

    public void cancel() {
        if (this.status != AdvertisementStatus.PUBLISHED &&
                this.status != AdvertisementStatus.PENDING_CANCEL) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.CANCELLED;
    }

    public void reject() {
        if (this.status != AdvertisementStatus.PENDING_APPROVAL &&
                this.status != AdvertisementStatus.PENDING_PAYMENT) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.REJECTED;
    }

    public void publish() {
        if (this.status != AdvertisementStatus.PENDING_PUBLISH) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.PUBLISHED;
    }

    public void denyCancel() {
        if (this.status != AdvertisementStatus.PENDING_CANCEL) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        if (!this.displayEndDate.isAfter(LocalDate.now())) {
            this.status = AdvertisementStatus.COMPLETED;
        }else if(!this.displayStartDate.isAfter(LocalDate.now())){
            this.status = AdvertisementStatus.PUBLISHED;
        }else{
            this.status = AdvertisementStatus.PENDING_PUBLISH;
        }
    }

    public void complete() {
        if (this.status != AdvertisementStatus.PUBLISHED) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.COMPLETED;
    }
    
    // 상태별 취소 처리
    public void cancelByStatus() {
        switch (this.status) {
            case PENDING_APPROVAL:
            case PENDING_PAYMENT:
                this.status = AdvertisementStatus.CANCELLED;
                break;
            case PUBLISHED:
            case PENDING_CANCEL:
                this.status = AdvertisementStatus.CANCELLED;
                break;
            default:
                throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
    }
    
    // 상태별 환불 신청 처리
    public void requestRefundByStatus() {
        switch (this.status) {
            case PENDING_PUBLISH:
            case PUBLISHED:
                this.status = AdvertisementStatus.PENDING_CANCEL;
                break;
            default:
                throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
    }
    
    // 기존 메서드들 (하위 호환용)
    public void cancelPendingApproval() {
        if (this.status != AdvertisementStatus.PENDING_APPROVAL) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.CANCELLED;
    }
    
    public void cancelPendingPayment() {
        if (this.status != AdvertisementStatus.PENDING_PAYMENT) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.CANCELLED;
    }
    
    // 상태 직접 변경 메서드
    public void updateStatus(AdvertisementStatus newStatus) {
        this.status = newStatus;
    }
    
    public void requestRefund() {
        if (this.status != AdvertisementStatus.PENDING_PUBLISH) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.PENDING_CANCEL;
    }
    
    public void requestPartialRefund() {
        if (this.status != AdvertisementStatus.PUBLISHED) {
            throw new CustomException(CustomErrorCode.INVALID_ADVERTISEMENT_STATUS);
        }
        this.status = AdvertisementStatus.PENDING_CANCEL;
    }
}
