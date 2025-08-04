package com.myce.system.entity;

import com.myce.system.entity.type.ChannelType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Getter
@Table(name = "system_template")
@NoArgsConstructor
public class TemplateSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_setting_id")
    private Long id;

    @Column(name = "editor_name", length = 50, nullable = true)
    private String editorName;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, columnDefinition = "VARCHAR(20)")
    private ChannelType channelType;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public TemplateSetting(String editorName, String name, ChannelType channelType, String message) {
        this.editorName = editorName;
        this.name = name;
        this.channelType = channelType;
        this.message = message;
    }
}
