package com.myce.expo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "expo_booth")
@Getter
@Setter
@NoArgsConstructor
public class ExpoBooth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expo_booth_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @Column(name = "booth_number", length = 255)
    private String boothNumber;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "main_image_url", length = 500)
    private String mainImageUrl;

    @Column(name = "contact_name", length = 30)
    private String contactName;

    @Column(name = "contact_phone", length = 11)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "is_premium")
    private Boolean isPremium;

    @Column(name = "display_rank")
    private Integer displayRank;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ExpoBooth(Expo expo, String boothNumber, String name, String description,
                     String mainImageUrl, String contactName, String contactPhone,
                     String contactEmail, Boolean isPremium, Integer displayRank) {
        this.expo = expo;
        this.boothNumber = boothNumber;
        this.name = name;
        this.description = description;
        this.mainImageUrl = mainImageUrl;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.isPremium = isPremium;
        this.displayRank = displayRank;
    }
}
