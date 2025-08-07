package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.ExpoAdminTicketRequestDto;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.service.ExpoAdminTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos/{expoId}/tickets")
@RequiredArgsConstructor
public class ExpoAdminTicketController {

    private final ExpoAdminTicketService service;

    @GetMapping//TODO:하위관리자
    public ResponseEntity<List<ExpoAdminTicketResponseDto>> getMyExpoTickets(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        return ResponseEntity.ok(service.getMyExpoTickets(expoId,memberId));
    }

    @DeleteMapping("/{ticketId}")//TODO:하위관리자
    public ResponseEntity<Void> deleteMyExpoTicket(
            @PathVariable Long expoId,
            @PathVariable Long ticketId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        service.deleteMyExpoTicket(expoId,memberId,ticketId);
        return ResponseEntity.ok().build();
    }

    @PostMapping//TODO:하위관리자
    public ResponseEntity<ExpoAdminTicketResponseDto> saveMyExpoTicket(
            @PathVariable Long expoId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ExpoAdminTicketRequestDto dto){
        Long memberId = customUserDetails.getMemberId();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveMyExpoTicket(expoId,memberId,dto));
    }

    @PutMapping("/{ticketId}")//TODO:하위관리자
    public ResponseEntity<ExpoAdminTicketResponseDto> updateMyExpoTicket(
            @PathVariable Long expoId,
            @PathVariable Long ticketId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ExpoAdminTicketRequestDto dto){
        Long memberId = customUserDetails.getMemberId();
        return ResponseEntity.ok(service.updateMyExpoTicket(expoId,memberId,ticketId,dto));
    }
}