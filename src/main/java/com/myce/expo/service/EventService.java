package com.myce.expo.service;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.auth.dto.type.LoginType;
import com.myce.expo.dto.EventRequest;
import com.myce.expo.dto.EventResponse;

import java.util.List;

public interface EventService {
    EventResponse saveEvent(Long expoId, EventRequest eventRequest, LoginType loginType, Long adminCodeId);

    List<EventResponse> getEvents(Long expoId, LoginType loginType, Long adminCodeId);

    List<EventResponse> getPublicEvents(Long expoId);

    EventResponse updateEvent(Long expoId, Long eventId, EventRequest eventRequest, LoginType loginType, Long adminCodeId);

    void deleteEvent(Long expoId, Long eventId, LoginType loginType, Long adminCodeId);


}
