package com.myce.expo.service;

import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoUpdateRequest;

public interface MyExpoService {
    MyExpoDetailResponse getMyExpoDetail(Long expoId, Long memberId);

    MyExpoDetailResponse updateMyExpoDetail(Long expoId, Long memberId, MyExpoUpdateRequest updateRequest);
}