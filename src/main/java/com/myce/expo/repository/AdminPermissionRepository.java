package com.myce.expo.repository;

import com.myce.expo.entity.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {
    // 해당 박람회의 관리 코드인지 확인 (권한 상관없이)
    boolean existsByAdminCodeIdAndAdminCodeExpoId(Long adminCodeId, Long expoId);
    
    boolean existsByAdminCodeIdAndAdminCodeExpoIdAndIsExpoDetailUpdateTrue(Long adminCodeId, Long expoId);
    boolean existsByAdminCodeIdAndAdminCodeExpoIdAndIsBoothInfoUpdateTrue(Long adminCodeId, Long expoId);
    boolean existsByAdminCodeIdAndAdminCodeExpoIdAndIsScheduleUpdateTrue(Long adminCodeId, Long expoId);
    boolean existsByAdminCodeIdAndAdminCodeExpoIdAndIsReserverListViewTrue(Long adminCodeId, Long expoId);
    boolean existsByAdminCodeIdAndAdminCodeExpoIdAndIsPaymentViewTrue(Long adminCodeId, Long expoId);
    boolean existsByAdminCodeIdAndAdminCodeExpoIdAndIsEmailLogViewTrue(Long adminCodeId, Long expoId);
    boolean existsByAdminCodeIdAndAdminCodeExpoIdAndIsOperationsConfigUpdateTrue(Long adminCodeId, Long expoId);
    boolean existsByAdminCodeIdAndAdminCodeExpoIdAndIsInquiryViewTrue(Long adminCodeId, Long expoId);
}