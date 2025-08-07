package com.myce.expo.repository;

import com.myce.expo.entity.AdminCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminCodeRepository extends JpaRepository<AdminCode, Long> {

    Optional<AdminCode> findByCode(String code);
}
