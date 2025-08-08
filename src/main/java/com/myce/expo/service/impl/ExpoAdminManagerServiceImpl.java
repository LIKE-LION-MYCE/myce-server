package com.myce.expo.service.impl;

import com.myce.expo.dto.ExpoAdminManagerResponse;
import com.myce.expo.entity.AdminCode;
import com.myce.expo.repository.AdminCodeRepository;
import com.myce.expo.service.ExpoAdminManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminManagerServiceImpl implements ExpoAdminManagerService {

    private final AdminCodeRepository adminCodeRepository;

    @Override
    public List<ExpoAdminManagerResponse> getMyExpoManagers(Long expoId, Long memberId) {

        List<AdminCode> adminCodes = adminCodeRepository.findByExpoId(expoId);

        return List.of();
    }
}
