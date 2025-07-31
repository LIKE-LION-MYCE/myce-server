package com.myce.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_grade_id", nullable = false)
    private MemberGrade memberGrade;

    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "login_id", length = 20)
    private String loginId;

    @Column(name = "password", length = 50)
    private String password;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "role", length = 20)
    private String role; // 또는 Enum 사용 시 EnumType.STRING

    @Column(name = "gender", length = 10)
    private String gender; // 또는 Enum 사용 시 EnumType.STRING

    @Column(name = "provider", length = 20)
    private String provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "mileage")
    private Integer mileage;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Builder
    public Member(MemberGrade memberGrade, String name, String loginId, String password,
                  String email, LocalDate birth, String role, String gender,
                  String provider, String providerId, Integer mileage, Boolean isDeleted) {
        this.memberGrade = memberGrade;
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.birth = birth;
        this.role = role;
        this.gender = gender;
        this.provider = provider;
        this.providerId = providerId;
        this.mileage = mileage;
        this.isDeleted = isDeleted;
    }
}
