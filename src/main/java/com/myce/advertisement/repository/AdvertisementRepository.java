package com.myce.advertisement.repository;

import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    Page<Advertisement> findByStatusIn(List<AdvertisementStatus> statuses, Pageable pageable);

    Page<Advertisement> findByTitleContainingAndStatusIn(String title, Collection<AdvertisementStatus> status, Pageable pageable);

    Page<Advertisement> findByTitleContainingAndStatus(String title, AdvertisementStatus status, Pageable pageable);
}
