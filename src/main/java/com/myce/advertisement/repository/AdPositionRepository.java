package com.myce.advertisement.repository;

import com.myce.advertisement.entity.AdPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdPositionRepository extends JpaRepository<AdPosition, Long> {
    Optional<AdPosition> findByName(String name);
}
