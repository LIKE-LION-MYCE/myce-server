package com.myce.expo.controller;

import com.myce.expo.dto.ExpoAdminTicketResponseDto;
import com.myce.expo.service.ExpoAdminTicketService;
import com.myce.expo.service.impl.ExpoAdminTicketServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expo-admin/my-expo/tickets")
@RequiredArgsConstructor
public class ExpoAdminTicketController {

    private final ExpoAdminTicketService service;

    @GetMapping
    public ResponseEntity<List<ExpoAdminTicketResponseDto>> getMyExpoTickets(){
        return ResponseEntity.ok(service.getMyExpoTickets());
    }
}