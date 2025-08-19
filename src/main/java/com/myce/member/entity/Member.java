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

@Entity
@Getter
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

    @Column(name = "login_id", length = 100, unique = true, nullable = false)
    private String loginId;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "phone", length = 13)
    private String phone;

    @Column(name = "birth")
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "VARCHAR(20)")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "VARCHAR(6)")
    private Gender gender;

    @Column(name = "mileage", nullable = false)
    private Integer mileage;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Member(MemberGrade memberGrade, String name, String loginId, String password, String email,
        LocalDate birth, String phone, Role role, Gender gender) {
        this.memberGrade = memberGrade;
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.birth = birth;
        this.role = role;
        this.gender = gender;
        this.mileage = 0;
        this.isDeleted = false;
    }

    public void withdraw() {
        this.isDeleted = true;
    }

    public void resetPassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateInfo (String phone, String email){
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = phone;
        }
        if (email != null && !email.trim().isEmpty()) {
            this.email = email;
        }
    }

    public void updateMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateMemberGrade(MemberGrade memberGrade) {
        this.memberGrade = memberGrade;
    }
}

