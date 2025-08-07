package com.myce.expo.service;

import com.myce.expo.dto.BoothRequest;
import com.myce.expo.dto.BoothResponse;

import java.util.List;

public interface BoothService {
    BoothResponse saveBooth(Long expoId, BoothRequest request, Long memberId);
    List<BoothResponse> getMyBooths(Long expoId, Long memberId);
    BoothResponse updateBooth(Long expoId, Long boothId, BoothRequest request, Long memberId);
}