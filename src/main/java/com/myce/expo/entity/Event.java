package com.myce.expo.entity;

import com.myce.expo.dto.EventRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "expo_event")
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expo_event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expo_id", nullable = false)
    private Expo expo;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "location", length = 30, nullable = false)
    private String location;

    @Column(name = "contact_name", length = 30, nullable = false)
    private String contactName;

    @Column(name = "contact_phone", length = 13, nullable = false)
    private String contactPhone;

    @Column(name = "contact_email", length = 100, nullable = false)
    private String contactEmail;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Builder
    public Event(Expo expo, String name, LocalDate eventDate, LocalTime startTime,
                 LocalTime endTime, String location, String contactName, String contactPhone,
                 String contactEmail, String description) {
        this.expo = expo;
        this.name = name;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.description = description;
    }

    public void update(EventRequest request) {
        this.name = request.getName();
        this.eventDate = request.getEventDate();
        this.startTime = request.getStartTime();
        this.endTime = request.getEndTime();
        this.location = request.getLocation();
        this.contactName = request.getContactName();
        this.contactPhone = request.getContactPhone();
        this.contactEmail = request.getContactEmail();
        this.description = request.getDescription();
    }
}
