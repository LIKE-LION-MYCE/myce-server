package com.myce.member.repository;

import com.myce.member.entity.MemberGrade;
import com.myce.member.entity.type.GradeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberGradeRepository extends JpaRepository<MemberGrade, Long> {

    Optional<MemberGrade> findByGradeCode(GradeCode gradeCode);
}
