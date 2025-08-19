package com.myce.advertisement.repository;

import com.myce.advertisement.entity.Advertisement;
import com.myce.advertisement.entity.type.AdvertisementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AdRepository extends JpaRepository<Advertisement, Long> {

    Page<Advertisement> findByStatusIn(List<AdvertisementStatus> statuses, Pageable pageable);

    Page<Advertisement> findByTitleContainingAndStatusIn(String title,
            Collection<AdvertisementStatus> status, Pageable pageable);

    Page<Advertisement> findByTitleContainingAndStatus(String title,
            AdvertisementStatus status, Pageable pageable);

    @Query("SELECT a FROM Advertisement a" +
            " WHERE a.displayStartDate <= :displayEndDate" +
            " AND a.displayEndDate >= :displayStartDate" +
            " AND a.status IN :status" +
            " AND a.adPosition.id = :adPositionId")
    List<Advertisement> findOverlappingAds(
            @Param("displayStartDate") LocalDate displayStartDate,
            @Param("displayEndDate") LocalDate displayEndDate,
            @Param("status") List<AdvertisementStatus> status,
            @Param("adPositionId") Long adPositionId);

    @Query("SELECT a FROM Advertisement a JOIN FETCH a.adPosition" +
            " WHERE a.member.id = :memberId ORDER BY a.createdAt DESC")
    Page<Advertisement> findByMemberIdWithAdPosition(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT a FROM Advertisement a JOIN FETCH a.adPosition" +
            " WHERE a.id = :advertisementId AND a.member.id = :memberId")
    Optional<Advertisement> findByIdAndMemberIdWithAdPosition(@Param("advertisementId") Long advertisementId,
            @Param("memberId") Long memberId);

    List<Advertisement> findAllByDisplayStartDateLessThanEqualAndStatus(
            LocalDate date, AdvertisementStatus status);

    List<Advertisement> findAllByDisplayEndDateLessThanAndStatus(
            LocalDate date, AdvertisementStatus status);

    @Query("SELECT a FROM Advertisement a" +
            " WHERE a.status IN :status" +
            " AND a.displayStartDate <= CURRENT_DATE" +
            " AND a.displayEndDate >= CURRENT_DATE")
    List<Advertisement> findAdsActiveTodayAndStatusIn(
            @Param("status") List<AdvertisementStatus> status);

    Optional<Advertisement> findByIdAndMemberId(Long advertisementId, Long memberId);


    @Query("SELECT COUNT(a) FROM Advertisement a " +
            "WHERE a.status NOT IN :statuses " +
            "AND a.createdAt BETWEEN :createdAtAfter AND :createdAtBefore")
    long countAllByCreatedAtBetween(List<AdvertisementStatus> statuses, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);
}
