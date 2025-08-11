package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.CongestionResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.dto.TicketSummaryResponse;
import com.myce.expo.entity.Ticket;
import com.myce.expo.service.ExpoService;
import com.myce.expo.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expos")
@RequiredArgsConstructor
public class ExpoController {
    private final ExpoService exposervice;
    private final TicketService ticketService;

    // 박람회 등록
    @PostMapping
    public ResponseEntity<Long> saveExpo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                         @RequestBody @Valid ExpoRegistrationRequest expoRegistrationRequest) {
        Long memberId = customUserDetails.getMemberId();
        exposervice.saveExpo(memberId, expoRegistrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    // 박람회 실시간 혼잡도 조회
    @GetMapping("/{expoId}/congestion")
    public ResponseEntity<CongestionResponse> getCongestionLevel(@PathVariable Long expoId) {
        CongestionResponse congestionResponse = exposervice.getCongestionLevel(expoId);
        return ResponseEntity.ok(congestionResponse);
    }

    // 박람회 티켓 조회(예매용)
    @GetMapping("/{expoId}/tickets/reservations")
    public ResponseEntity<List<TicketSummaryResponse>> getTickets(@PathVariable Long expoId) {
        return ResponseEntity.ok(ticketService.getTickets(expoId));
    }
}
