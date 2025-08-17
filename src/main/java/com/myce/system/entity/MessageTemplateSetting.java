package com.myce.system.entity;

import com.myce.member.entity.Member;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Getter
@Table(name = "message_template_setting")
@NoArgsConstructor
public class MessageTemplateSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_setting_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="template_code", length = 50, nullable = false)
    private MessageTemplateCode code;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private ChannelType channelType;

    @Column(name = "subject", length = 100, nullable = false)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "use_image", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean useImage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    public void updateTemplate(String name, String subject, String content) {
        this.name = name;
        this.subject = subject;
        this.content = content;
    }
}
