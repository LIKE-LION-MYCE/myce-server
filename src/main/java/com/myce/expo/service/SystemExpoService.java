package com.myce.expo.service;

import java.time.LocalDate;

public interface SystemExpoService {
    void checkAvailablePeriod(Long expoId, LocalDate startedAt, LocalDate endedAt);
    
    int publishPendingExpos();
    
    int closeCompletedExpos();
    
    void refreshExpoCache();
}