package com.myce.expo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "admin_permission")
@EntityListeners(AuditingEntityListener.class)
public class AdminPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_permission_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_admin_code_id", nullable = false)
    private AdminCode adminCode;

    @Column(name = "is_expo_detail_update", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isExpoDetailUpdate;

    @Column(name = "is_booth_info_update", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isBoothInfoUpdate;

    @Column(name = "is_schedule_update", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isScheduleUpdate;

    @Column(name = "is_reserver_list_view", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isReserverListView;

    @Column(name = "is_payment_view", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isPaymentView;

    @Column(name = "is_email_log_view", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isEmailLogView;

    @Column(name = "is_operations_config_update", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isOperationsConfigUpdate;

    @Column(name = "is_inquiry_view", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isInquiryView;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public AdminPermission(AdminCode adminCode, Boolean isExpoDetailUpdate, Boolean isBoothInfoUpdate,
                           Boolean isScheduleUpdate, Boolean isReserverListView, Boolean isPaymentView,
                           Boolean isEmailLogView, Boolean isOperationsConfigUpdate, Boolean isInquiryView) {
        adminCode.setAdminPermission(this);
        this.adminCode = adminCode;
        this.isExpoDetailUpdate = isExpoDetailUpdate;
        this.isBoothInfoUpdate = isBoothInfoUpdate;
        this.isScheduleUpdate = isScheduleUpdate;
        this.isReserverListView = isReserverListView;
        this.isPaymentView = isPaymentView;
        this.isEmailLogView = isEmailLogView;
        this.isOperationsConfigUpdate = isOperationsConfigUpdate;
        this.isInquiryView = isInquiryView;
    }

    public void updateAdminPermission(Boolean isExpoDetailUpdate, Boolean isBoothInfoUpdate,
                                      Boolean isScheduleUpdate, Boolean isReserverListView,
                                      Boolean isPaymentView, Boolean isEmailLogView,
                                      Boolean isOperationsConfigUpdate,Boolean isInquiryView){
        this.isExpoDetailUpdate = isExpoDetailUpdate;
        this.isBoothInfoUpdate = isBoothInfoUpdate;
        this.isScheduleUpdate = isScheduleUpdate;
        this.isReserverListView = isReserverListView;
        this.isPaymentView = isPaymentView;
        this.isEmailLogView = isEmailLogView;
        this.isOperationsConfigUpdate = isOperationsConfigUpdate;
        this.isInquiryView = isInquiryView;
    }
}