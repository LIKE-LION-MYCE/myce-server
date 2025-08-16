package com.myce.reservation.service;

import com.myce.reservation.dto.GuestReservationRequest;

public interface GuestReservationService {
  void updateGuestId(GuestReservationRequest request);
}
