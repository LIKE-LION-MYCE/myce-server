package com.myce.expo.service;

import com.myce.expo.dto.*;

import java.util.List;

public interface ExpoService {

    void saveExpo(Long memberId, ExpoRegistrationRequest request);
    
    CongestionResponse getCongestionLevel(Long expoId);
    
    // 분리된 상세 정보 조회 메서드들
    ExpoBasicResponse getExpoBasicInfo(Long expoId);

    ExpoBookmarkResponse getExpoBookmarkStatus(Long expoId, Long memberId);
    
    ExpoReviewsResponse getExpoReviews(Long expoId, Long memberId, int page, int size);
    
    ExpoLocationResponse getExpoLocation(Long expoId);
    
    List<BoothResponse> getExpoBooths(Long expoId);
}

