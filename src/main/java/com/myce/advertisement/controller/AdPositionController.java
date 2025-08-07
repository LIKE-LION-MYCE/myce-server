package com.myce.advertisement.controller;

import com.myce.advertisement.dto.AdPositionResponse;
import com.myce.advertisement.service.AdPositionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ad-positions")
@RequiredArgsConstructor
public class AdPositionController {
  private final AdPositionService adPositionService;

  // 광고 배너 위치 목록 조회
  @GetMapping
  public ResponseEntity<List<AdPositionResponse>> getAllAdPositions() {
    List<AdPositionResponse> adPositions = adPositionService.getAdPositions();
    return ResponseEntity.ok().body(adPositions);
  }
}
