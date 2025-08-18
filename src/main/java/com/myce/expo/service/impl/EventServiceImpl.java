package com.myce.expo.service.impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
import com.myce.expo.dto.EventRequest;
import com.myce.expo.dto.EventResponse;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.entity.Event;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.EventRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.service.EventService;
import com.myce.expo.service.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ExpoRepository expoRepository;
    private final EventMapper eventMapper;
    private final AdminCodeRepository adminCodeRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final ExpoAdminAccessValidate expoAdminAccessValidate;

    // 행사 등록
    @Override
    public EventResponse saveEvent(Long expoId, EventRequest eventRequest, LoginType loginType, Long principalId) {
        // 접근 권한 검증
        expoAdminAccessValidate.ensureEditable(expoId, principalId, loginType, ExpoAdminPermission.SCHEDULE_UPDATE);

        // 운영중인 박람회 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_EXIST));

        // 행사 요청을 엔티티로 변환
        Event event = eventMapper.toEntity(eventRequest, expo);

        // 행사 엔티티 저장
        Event savedEvent = eventRepository.save(event);

        // 행사 응답 dto 반환
        return eventMapper.toResponse(savedEvent);
    }

    // 행사 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEvents(Long expoId, LoginType loginType, Long principalId) {
        // 접근 권한 검증
        expoAdminAccessValidate.ensureViewable(expoId, principalId, loginType, ExpoAdminPermission.SCHEDULE_UPDATE);

        // 행사 엔티티 목록 조회
        List<Event> events = eventRepository.findAllByExpoId(expoId);

        // 행사 엔티티를 응답 dto로 매핑하여 리스트로 반환
        return events.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 행사 목록 조회 (공개용 - 비회원 접근 가능)
    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getPublicEvents(Long expoId) {
        // 행사 엔티티 목록 조회 (권한 검증 없음)
        List<Event> events = eventRepository.findAllByExpoIdOrderByEventDateAscStartTimeAsc(expoId);

        // 행사 엔티티를 응답 dto로 매핑하여 리스트로 반환
        return events.stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 행사 수정
    @Override
    public EventResponse updateEvent(Long expoId, Long eventId, EventRequest eventRequest, LoginType loginType, Long principalId) {
        // 접근 권한 검증
        expoAdminAccessValidate.ensureEditable(expoId, principalId, loginType, ExpoAdminPermission.SCHEDULE_UPDATE);

        // 행사 검증 후 검증된 행사 엔티티 반환
        Event event = getEventAndValidate(expoId, eventId);

        // 행사 엔티티에서 직접 필드 수정
        event.update(eventRequest);

        // 수정된 엔티티 응답 DTO에 매핑하여 반환
        return eventMapper.toResponse(event);
    }

    // 행사 삭제
    @Override
    public void deleteEvent(Long expoId, Long eventId, LoginType loginType, Long principalId) {
        // 접근 권한 검증
        expoAdminAccessValidate.ensureEditable(expoId, principalId, loginType, ExpoAdminPermission.SCHEDULE_UPDATE);

        // 행사 검증 후 검증된 행사 엔티티 반환
        Event event = getEventAndValidate(expoId, eventId);

        // 행사 삭제
        eventRepository.delete(event);
    }


    // 행사 검증 후 검증된 행사 엔티티 반환
    private Event getEventAndValidate(Long expoId, Long eventId) {
        // 행사 엔티티 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EVENT_NOT_FOUND));

        // 행사가 해당 박람회에 속했는지 검증
        if (!event.getExpo().getId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.EVENT_NOT_BELONG_TO_EXPO);
        }
        return event;
    }

}
