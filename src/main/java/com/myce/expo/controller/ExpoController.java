package com.myce.expo.controller;

import com.myce.auth.dto.CustomUserDetails;
import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.service.ExpoService;
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
@RequestMapping("/api/expos")
@RequiredArgsConstructor
public class ExpoController {
  private final ExpoService exposervice;

  // 박람회 등록
  @PostMapping
  public ResponseEntity<Long> saveExpo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @RequestBody @Valid ExpoRegistrationRequest expoRegistrationRequest){
    Long memberId = customUserDetails.getMemberId();
    exposervice.saveExpo(memberId, expoRegistrationRequest);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
