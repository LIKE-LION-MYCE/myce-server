package com.myce.advertisement.service;


import com.myce.advertisement.dto.AdvertisementRegistrationRequest;

public interface UserAdvertisementService {
  void saveAdvertisement(Long memberId, AdvertisementRegistrationRequest request);
}
