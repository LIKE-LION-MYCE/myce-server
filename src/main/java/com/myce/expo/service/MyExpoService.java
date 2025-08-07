package com.myce.expo.service;

import com.myce.expo.dto.MyExpoDetailResponse;
import com.myce.expo.dto.MyExpoUpdateRequest;

import java.util.List;

public interface MyExpoService {
    MyExpoDetailResponse getMyExpoDetail(Long expoId, Long memberId);

    List<Long> getMyExpos(Long memberId);

    MyExpoDetailResponse updateMyExpoDetail(Long expoId, Long memberId, MyExpoUpdateRequest updateRequest);
}