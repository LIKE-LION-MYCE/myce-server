package com.myce.expo.service;

import com.myce.expo.dto.CongestionResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;

public interface ExpoService {

    void saveExpo(Long memberId, ExpoRegistrationRequest request);
    
    CongestionResponse getCongestionLevel(Long expoId);
}

