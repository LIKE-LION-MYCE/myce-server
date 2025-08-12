package com.myce.expo.service;

import com.myce.auth.dto.type.LoginType;
import com.myce.expo.dto.BoothRequest;
import com.myce.expo.dto.BoothResponse;

import java.util.List;

public interface BoothService {
    BoothResponse saveBooth(Long expoId, BoothRequest request, LoginType loginType, Long principalId);
    List<BoothResponse> getMyBooths(Long expoId, LoginType loginType, Long principalId);
    BoothResponse updateBooth(Long expoId, Long boothId, BoothRequest request, LoginType loginType, Long principalId);
    void deleteBooth(Long expoId, Long boothId, LoginType loginType, Long principalId);
}
