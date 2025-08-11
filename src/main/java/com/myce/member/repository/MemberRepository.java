package com.myce.member.repository;

import com.myce.member.entity.Member;
import com.myce.member.entity.type.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderTypeAndLoginId(ProviderType providerType, String loginId);

    Optional<Member> findByNameAndEmail(String name, String email);

    @Query("SELECT m FROM Member m WHERE m.id = (SELECT e.member.id FROM Expo e WHERE e.id=:expoId)")
    Optional<Member> findByExpoId(Long expoId);
}