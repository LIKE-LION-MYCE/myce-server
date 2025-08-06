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
@RequestMapping("/api/expo-admin/my-expo/tickets")
@RequiredArgsConstructor
public class ExpoAdminTicketController {

    private final ExpoAdminTicketService service;

    @GetMapping//TODO:하위관리자
    public ResponseEntity<List<ExpoAdminTicketResponseDto>> getMyExpoTickets(
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        return ResponseEntity.ok(service.getMyExpoTickets(memberId));
    }

    @DeleteMapping("/{ticketId}")//TODO:하위관리자
    public ResponseEntity<Void> deleteMyExpoTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        service.deleteMyExpoTicket(memberId,ticketId);
        return ResponseEntity.ok().build();
    }

    @PostMapping//TODO:하위관리자
    public ResponseEntity<Void> saveMyExpoTicket(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody ExpoAdminTicketRequestDto dto){
        Long memberId = customUserDetails.getMemberId();
        service.saveMyExpoTicket(memberId,dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{ticketId}")//TODO:하위관리자
    public ResponseEntity<Void> updateMyExpoTicket(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long ticketId,
            @Valid @RequestBody ExpoAdminTicketRequestDto dto){
        Long memberId = customUserDetails.getMemberId();
        service.updateMyExpoTicket(memberId,ticketId,dto);
        return ResponseEntity.ok().build();
    }
}