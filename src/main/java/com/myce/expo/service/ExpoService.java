package com.myce.expo.service;

import com.myce.expo.dto.CongestionResponse;
import com.myce.expo.dto.ExpoCardResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ExpoService {

    void saveExpo(Long memberId, ExpoRegistrationRequest request);
    
    CongestionResponse getCongestionLevel(Long expoId);

    // 박람회 카드 리스트 조회
    List<ExpoCardResponse> getExpoCardsFiltered(
        Long memberId, String categoryName, LocalDate from, LocalDate to, String keyword, Pageable pageable);
}