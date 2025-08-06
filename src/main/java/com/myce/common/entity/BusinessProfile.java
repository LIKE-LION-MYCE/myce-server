package com.myce.common.entity;

import com.myce.common.entity.type.TargetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(
        name = "business_profile",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UniqueTargetTypeTargetId",
                        columnNames = {"target_type", "target_id"})
        }
)
@EntityListeners(AuditingEntityListener.class)
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_profile_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "company_name", length = 100, nullable = false)
    private String companyName;

    @Column(name = "ceo_name", length = 20, nullable = false)
    private String ceoName;

    @Column(name = "address", length = 300, nullable = false)
    private String address;

    @Column(name = "contact_phone", length = 13, nullable = false)
    private String contactPhone;

    @Column(name = "contact_email", length = 100, nullable = false)
    private String contactEmail;

    @Column(name = "business_registration_number", length = 50, nullable = false)
    private String businessRegistrationNumber;

    @Column(name = "logo_url", length = 500, nullable = true)
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public BusinessProfile(TargetType targetType, Long targetId, String companyName,
                           String address, String ceoName, String contactEmail, String contactPhone,
                            String businessRegistrationNumber) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.companyName = companyName;
        this.address = address;
        this.ceoName = ceoName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public void updateProfileInfo(String logoUrl, String companyName, String address, String ceoName,
                              String contactEmail, String contactPhone, String businessRegistrationNumber) {
        this.logoUrl = logoUrl;
        this.companyName = companyName;
        this.address = address;
        this.ceoName = ceoName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.businessRegistrationNumber = businessRegistrationNumber;
    }
}