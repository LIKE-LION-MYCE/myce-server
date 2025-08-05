package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.service.ExpoAdminTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expo-admin/my-expo/tickets")
@RequiredArgsConstructor
public class ExpoAdminTicketController {

    private final ExpoAdminTicketService service;

    @GetMapping
    public ResponseEntity<List<ExpoAdminTicketResponseDto>> getMyExpoTickets(
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId(); //TODO:하위관리자
        return ResponseEntity.ok(service.getMyExpoTickets(memberId));
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteMyExpoTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Long memberId = customUserDetails.getMemberId();
        service.deleteMyExpoTicket(memberId,ticketId); //TODO:하위관리자
        return ResponseEntity.noContent().build();
    }
}