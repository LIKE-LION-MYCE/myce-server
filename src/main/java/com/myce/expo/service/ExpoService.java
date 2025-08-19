package com.myce.expo.service;

import com.myce.expo.dto.*;

import java.util.List;
import com.myce.expo.dto.CongestionResponse;
import com.myce.expo.dto.ExpoCardResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpoService {

    void saveExpo(Long memberId, ExpoRegistrationRequest request);
    
    CongestionResponse getCongestionLevel(Long expoId);

    // 분리된 상세 정보 조회 메서드들
    ExpoBasicResponse getExpoBasicInfo(Long expoId);

    ExpoBookmarkResponse getExpoBookmarkStatus(Long expoId, Long memberId);

    ExpoReviewsResponse getExpoReviews(Long expoId, int page, int size);

    ExpoLocationResponse getExpoLocation(Long expoId);

    List<BoothResponse> getExpoBooths(Long expoId);

    // 박람회 카드 리스트 조회
    Page<ExpoCardResponse> getExpoCardsFiltered(
            Long memberId, String categoryName, String status, LocalDate from, LocalDate to, String keyword, Pageable pageable);
}

