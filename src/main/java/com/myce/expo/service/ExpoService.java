package com.myce.expo.service;

import com.myce.expo.dto.CongestionResponse;
import com.myce.expo.dto.ExpoCardResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;
import java.util.List;

public interface ExpoService {

    void saveExpo(Long memberId, ExpoRegistrationRequest request);
    
    CongestionResponse getCongestionLevel(Long expoId);

    // 박람회 카드 리스트 조회
    List<ExpoCardResponse> getExpoCards(Long memberId);
}

