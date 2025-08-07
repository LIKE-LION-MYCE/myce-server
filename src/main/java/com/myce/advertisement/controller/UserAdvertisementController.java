package com.myce.advertisement.controller;

import com.myce.advertisement.dto.AdvertisementRegistrationRequest;
import com.myce.advertisement.service.UserAdvertisementService;
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
public class UserAdvertisementController {
  private final UserAdvertisementService userAdvertisementService;

  // 광고 등록
  @PostMapping
  public ResponseEntity<Long> saveAdvertisement(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody @Valid AdvertisementRegistrationRequest advertisementRegistrationRequest){
    Long memberId = customUserDetails.getMemberId();
    userAdvertisementService.saveAdvertisement(memberId, advertisementRegistrationRequest);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
