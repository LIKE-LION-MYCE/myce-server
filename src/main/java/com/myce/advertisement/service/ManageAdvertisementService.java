package com.myce.advertisement.service;


import java.time.LocalDate;

public interface ManageAdvertisementService {
    void checkAvailablePeriod(Long locationId,
            LocalDate startedAt, LocalDate endedAt);
}