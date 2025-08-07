package com.myce.member.repository;

import com.myce.member.entity.MemberSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberSettingRepository extends JpaRepository<MemberSetting, Long> {
    
    Optional<MemberSetting> findByMemberId(Long memberId);
}