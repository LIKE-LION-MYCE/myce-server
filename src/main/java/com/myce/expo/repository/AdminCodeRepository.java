package com.myce.expo.repository;

import com.myce.expo.entity.AdminCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminCodeRepository extends JpaRepository<AdminCode, Long> {

    Optional<AdminCode> findByCode(String code);

    List<AdminCode> findByExpoId(@Param("expoId") Long expoId);

    @Query("SELECT ac FROM AdminCode ac JOIN FETCH ac.adminPermission WHERE ac.expoId = :expoId")
    List<AdminCode> findAllWithAdminPermissionByExpoId(@Param("expoId") Long expoId);

    @Query("""
        SELECT ac FROM AdminCode ac
        JOIN FETCH ac.adminPermission
        WHERE ac.id IN :ids
    """)
    List<AdminCode> findAllWithAdminPermissionByIds(@Param("ids") List<Long> ids);

    @Query("""
        SELECT ac FROM AdminCode ac
        JOIN FETCH ac.adminPermission
        WHERE ac.id = :id
    """)
    Optional<AdminCode> findWithAdminPermissionById(@Param("id") Long id);
}