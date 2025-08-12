package com.myce.expo.service.mapper;

import com.myce.expo.dto.EventRequest;
import com.myce.expo.dto.EventResponse;
import com.myce.expo.entity.Event;
import com.myce.expo.entity.Expo;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(EventRequest request, Expo expo) {
        return Event.builder()
                .expo(expo)
                .name(request.getName())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .description(request.getDescription())
                .build();
    }

    public EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .contactName(event.getContactName())
                .contactPhone(event.getContactPhone())
                .contactEmail(event.getContactEmail())
                .description(event.getDescription())
                .build();
    }
}
