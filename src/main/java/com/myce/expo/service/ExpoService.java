package com.myce.expo.service;

import com.myce.expo.dto.ExpoRegistrationRequest;

public interface ExpoService {
  Long registerExpo(ExpoRegistrationRequest request);
}

