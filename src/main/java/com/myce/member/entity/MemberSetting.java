package com.myce.member.entity;

import com.myce.member.entity.type.FontSize;
import com.myce.member.entity.type.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@Table(name = "member_setting")
@EntityListeners(AuditingEntityListener.class)
public class MemberSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_setting_id")
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

    @Column(name = "is_receive_email", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isReceiveEmail;

    @Column(name = "is_receive_push", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isReceivePush;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public MemberSetting(Member member, Language language, FontSize fontSize,
                         Boolean isReceiveEmail, Boolean isReceivePush) {
        this.member = member;
        this.language = language;
        this.fontSize = fontSize;
        this.isReceiveEmail = isReceiveEmail;
        this.isReceivePush = isReceivePush;
    }

    public void updateSettings(Language language, FontSize fontSize) {
        this.language = language;
        this.fontSize = fontSize;
    }
}
