package com.myce.expo.service;

import com.myce.expo.dto.BoothRegistrationRequest;
import com.myce.expo.dto.BoothRegistrationResponse;

public interface BoothService {
    BoothRegistrationResponse saveBooth(Long expoId, BoothRegistrationRequest request, Long memberId);
}
