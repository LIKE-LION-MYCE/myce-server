package com.myce.expo.entity;

import com.myce.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "expo_admin_code")
@EntityListeners(AuditingEntityListener.class)
public class AdminCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expo_admin_code_id")
    private Long id;

    @Setter
    @OneToOne(mappedBy = "adminCode")
    private AdminPermission adminPermission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_member_id", nullable = false)
    private Member superMember;

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Column(name = "expired_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Builder
    public AdminCode(Expo expo, Member member, String code, LocalDateTime expiredAt) {
        this.expo = expo;
        this.superMember = member;
        this.code = code;
        this.expiredAt = expiredAt;
    }

}
