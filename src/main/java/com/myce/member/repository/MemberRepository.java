package com.myce.member.repository;

import com.myce.member.entity.Member;
import com.myce.member.entity.type.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByProviderTypeAndLoginId(ProviderType providerType, String loginId);
}
