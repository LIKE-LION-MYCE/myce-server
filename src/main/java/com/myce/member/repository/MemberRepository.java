package com.myce.member.repository;

import com.myce.member.entity.Member;
import com.myce.member.entity.type.ProviderType;
import com.myce.member.entity.type.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByNameAndEmail(String name, String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNameAndEmailAndLoginId(String name, String email, String loginId);

    Page<Member> findByRole(Role role, Pageable pageable);

    boolean existsByLoginId(String loginId);

    Optional<Member> findByLoginId(String loginId);

    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.id = (SELECT e.member.id FROM Expo e WHERE e.id=:expoId)")
    Optional<Member> findByExpoId(Long expoId);

    @Query("SELECT m FROM Member m " +
            "WHERE m.loginId LIKE %:keyword% " +
            "OR m.name LIKE %:keyword%")
    Page<Member> findAllByKeywordContaining(String keyword, Pageable pageable);
}