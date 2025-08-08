package com.myce.expo.repository;

import com.myce.expo.entity.AdminCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminCodeRepository extends JpaRepository<AdminCode, Long> {

    Optional<AdminCode> findByCode(String code);
    
    @Query("SELECT ac FROM AdminCode ac WHERE ac.expo.id = :expoId ORDER BY ac.createdAt DESC LIMIT 5")
    List<AdminCode> findTop5ByExpoIdOrderByCreatedAtDesc(@Param("expoId") Long expoId);
}
