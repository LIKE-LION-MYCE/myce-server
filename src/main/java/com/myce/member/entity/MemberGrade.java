package com.myce.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_grade")
@Getter
@Setter
@NoArgsConstructor
public class MemberGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_grade_id")
    private Long id;

    @Column(name = "grade_code", length = 20)
    private String gradeCode; // 예: BASIC, GOLD, VIP

    @Column(name = "grade_name", length = 50, nullable = false)
    private String gradeName; // 예: 일반회원, 골드, VIP

    @Column(name = "mileage_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal mileageRate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public MemberGrade(String gradeCode, String gradeName, BigDecimal mileageRate,
                       String description, Boolean isActive) {
        this.gradeCode = gradeCode;
        this.gradeName = gradeName;
        this.mileageRate = mileageRate;
        this.description = description;
        this.isActive = isActive;
    }
}
