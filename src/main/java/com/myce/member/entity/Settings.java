package com.myce.member.entity;

import com.myce.member.entity.code.FontSize;
import com.myce.member.entity.code.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 10, nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(name = "font_size", length = 20, nullable = false)
    private FontSize fontSize;

    @Column(name = "is_receive_email")
    private Boolean isReceiveEmail;

    @Column(name = "is_receive_push")
    private Boolean isReceivePush;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Settings(Member member, Language language, FontSize fontSize,
                    Boolean isReceiveEmail, Boolean isReceivePush) {
        this.member = member;
        this.language = language;
        this.fontSize = fontSize;
        this.isReceiveEmail = isReceiveEmail;
        this.isReceivePush = isReceivePush;
    }
}
