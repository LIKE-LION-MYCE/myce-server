package com.myce.member.repository;

import com.myce.member.entity.Favorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByMemberId(Long MemberId);

    boolean existsByMemberIdAndExpoId(Long memberId, Long expoId);
}