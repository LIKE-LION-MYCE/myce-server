package com.myce.expo.service;

import com.myce.expo.dto.TicketQuantityRequest;
import com.myce.expo.dto.TicketSummaryResponse;
import java.util.List;

public interface TicketService {
  // 엑스포 티켓 정보 가져오기
  public List<TicketSummaryResponse> getTickets(Long expoId);

  // 티켓 수량 빼기
  void updateRemainingQuantity(TicketQuantityRequest request);
}
