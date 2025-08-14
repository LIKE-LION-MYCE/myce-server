package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.EventRequest;
import com.myce.expo.dto.EventResponse;
import com.myce.expo.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // 행사 등록
    @PostMapping
    public ResponseEntity<EventResponse> saveEvent(
            @PathVariable Long expoId,
            @Valid @RequestBody EventRequest eventRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        EventResponse response = eventService.saveEvent(expoId, eventRequest, userDetails.getLoginType(), userDetails.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 행사 목록 조회 (관리자용)
    @GetMapping("/admin")
    public ResponseEntity<List<EventResponse>> getEvents(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<EventResponse> events = eventService.getEvents(expoId, userDetails.getLoginType(), userDetails.getMemberId());
        return ResponseEntity.ok(events);
    }

    // 행사 목록 조회 (공개용 - 비회원 접근 가능)
    @GetMapping
    public ResponseEntity<List<EventResponse>> getPublicEvents(@PathVariable Long expoId) {
        List<EventResponse> events = eventService.getPublicEvents(expoId);
        return ResponseEntity.ok(events);
    }

    // 행사 수정
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long expoId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequest eventRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        EventResponse response = eventService.updateEvent(expoId, eventId, eventRequest, userDetails.getLoginType(), userDetails.getMemberId());
        return ResponseEntity.ok(response);
    }

    //행사 삭제
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long expoId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.deleteEvent(expoId, eventId, userDetails.getLoginType(), userDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
