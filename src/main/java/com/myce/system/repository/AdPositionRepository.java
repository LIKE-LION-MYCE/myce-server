package com.myce.system.repository;

import com.myce.system.entity.AdPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdPositionRepository extends JpaRepository<AdPosition,Long> {
    Page<AdPosition> findAll(Pageable pageable);
    
    List<AdPosition> findAllByIsActiveTrue();
}