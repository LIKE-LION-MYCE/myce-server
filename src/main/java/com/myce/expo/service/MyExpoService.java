package com.myce.expo.service;

import com.myce.expo.dto.MyExpoDetailResponse;

public interface MyExpoService {
    MyExpoDetailResponse getMyExpoDetail(Long expoId, Long memberId);
}
