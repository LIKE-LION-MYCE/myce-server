package com.myce.member.repository;

import com.myce.member.entity.Favorite;
import com.myce.member.entity.Member;
import com.myce.member.entity.type.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByMemberId(Long MemberId);
}