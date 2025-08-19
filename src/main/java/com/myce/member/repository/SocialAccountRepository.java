package com.myce.member.repository;

import com.myce.member.entity.Member;
import com.myce.member.entity.SocialAccount;
import com.myce.member.entity.type.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    @Query("SELECT s FROM SocialAccount s JOIN FETCH s.member "
            + "WHERE s.providerType = :providerType AND s.providerId = :providerId")
    Optional<SocialAccount> findByProviderTypeAndProviderId(ProviderType providerType, String providerId);
}