package com.myce.member.entity;

import com.myce.member.entity.type.GradeCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "member_grade")
@EntityListeners(AuditingEntityListener.class)
public class MemberGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_grade_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_code", nullable = false, columnDefinition = "VARCHAR(40)")
    private GradeCode gradeCode;

    @Column(name = "mileage_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal mileageRate;

    @Column(name = "base_amount")
    private Integer baseAmount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "grade_image_url", length = 500, nullable = false)
    private String gradeImageUrl;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;


    @Builder
    public MemberGrade(GradeCode gradeCode, BigDecimal mileageRate, Integer baseAmount,
                       String description, String gradeImageUrl, Boolean isActive) {
        this.gradeCode = gradeCode;
        this.mileageRate = mileageRate;
        this.baseAmount = baseAmount;
        this.description = description;
        this.gradeImageUrl = gradeImageUrl;
        this.isActive = isActive;
    }
}
