package com.myce.advertisement.controller;

import com.myce.advertisement.dto.AdRegistrationRequest;
import com.myce.advertisement.service.UserAdService;
import com.myce.auth.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class UserAdController {
  private final UserAdService userAdService;

  // 광고 등록
  @PostMapping
  public ResponseEntity<Long> saveAdvertisement(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody @Valid AdRegistrationRequest adRegistrationRequest){
    Long memberId = customUserDetails.getMemberId();
    userAdService.saveAdvertisement(memberId, adRegistrationRequest);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
