package com.myce.member.repository;

import com.myce.member.entity.Favorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByMemberId(Long MemberId);

    @Query("SELECT f.expo.id FROM Favorite f WHERE f.member.id = :memberId")
    List<Long> findExpoIdsByMemberId(@Param("memberId") Long memberId);

    boolean existsByMember_IdAndExpo_Id(Long memberId, Long expoId);

    void deleteByMember_IdAndExpo_Id(Long memberId, Long expoId);
}