package com.myce.member.entity;

import com.myce.member.entity.type.Gender;
import com.myce.member.entity.type.ProviderType;
import com.myce.member.entity.type.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "member")
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_grade_id", nullable = false)
    private MemberGrade memberGrade;

    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @Column(name = "login_id", length = 20, nullable = false)
    private String loginId;

    @Column(name = "password", length = 200, nullable = false)
    private String password;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "VARCHAR(20)")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, columnDefinition = "VARCHAR(6)")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, columnDefinition = "VARCHAR(20)")
    private ProviderType providerType;

    @Column(name = "provider_id", length = 100, nullable = false)
    private String providerId;

    @Column(name = "mileage", nullable = false)
    private Integer mileage;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Member(MemberGrade memberGrade, String name, String loginId, String password,
                  String email, LocalDate birth, Role role, Gender gender,
                  ProviderType providerType, String providerId, Integer mileage, Boolean isDeleted) {
        this.memberGrade = memberGrade;
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.birth = birth;
        this.role = role;
        this.gender = gender;
        this.providerType = providerType;
        this.providerId = providerId;
        this.mileage = mileage;
        this.isDeleted = isDeleted;
    }
}
