package com.myce.expo.service;

import com.myce.expo.dto.ExpoRegistrationRequest;

import java.util.List;

public interface ExpoService {
  void saveExpo(Long memberId, ExpoRegistrationRequest request);
  List<Long> getMyExpos(Long memberId);
}

