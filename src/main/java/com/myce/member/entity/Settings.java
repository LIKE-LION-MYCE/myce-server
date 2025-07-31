package com.myce.member.entity;

import com.myce.member.entity.type.FontSize;
import com.myce.member.entity.type.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "settings")
@EntityListeners(AuditingEntityListener.class)
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, columnDefinition = "VARCHAR(10)")
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(name = "font_size", nullable = false, columnDefinition = "VARCHAR(20)")
    private FontSize fontSize;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Settings(Member member, Language language, FontSize fontSize) {
        this.member = member;
        this.language = language;
        this.fontSize = fontSize;
    }
}
