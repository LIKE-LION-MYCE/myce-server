package com.myce.reservation.service;

import com.myce.reservation.dto.ResolveReserversRequest;
import com.myce.reservation.dto.ResolveReserversResponse;

public interface ReserverResolveService {
  ResolveReserversResponse resolve(ResolveReserversRequest request);
}
