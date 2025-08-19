package com.myce.member.entity;

import com.myce.member.entity.type.ProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "social_account", 
    uniqueConstraints = {
        @UniqueConstraint(
            name = "PROVIDER_TYPE_AND_PROVIDER_ID_UNIQUE",
            columnNames = {"provider_type", "provider_id"}
        )
    },
    indexes = {
        @Index(name = "IDX_MEMBER_PROVIDER", columnList = "member_id, provider_type"),
        @Index(name = "IDX_MEMBER_ID", columnList = "member_id"),
        @Index(name = "IDX_PROVIDER_LOOKUP", columnList = "provider_type, provider_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private ProviderType providerType;

    @Column(name = "provider_id", length = 100, nullable = false)
    private String providerId;

    @Column(name = "provider_email", length = 100, nullable = false)
    private String providerEmail;

    @Column(name = "provider_name", length = 20, nullable = false)
    private String providerName;

    @CreationTimestamp
    @Column(name = "connected_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime connectedAt;

    @Builder
    public SocialAccount
            (Member member, ProviderType providerType, String providerId, String providerEmail, String providerName) {
        this.member = member;
        this.providerType = providerType;
        this.providerId = providerId;
        this.providerEmail = providerEmail;
        this.providerName = providerName;
    }
}