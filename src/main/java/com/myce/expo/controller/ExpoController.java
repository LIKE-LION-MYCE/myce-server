package com.myce.expo.controller;

import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.service.ExpoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<Long> registerExpo(@RequestBody ExpoRegistrationRequest expoRegistrationRequest){
    exposervice.registerExpo(expoRegistrationRequest);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
