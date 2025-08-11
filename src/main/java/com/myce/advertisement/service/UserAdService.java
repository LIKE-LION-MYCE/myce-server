package com.myce.advertisement.service;


import com.myce.advertisement.dto.AdRegistrationRequest;

public interface UserAdService {
  void saveAdvertisement(Long memberId, AdRegistrationRequest request);
}
