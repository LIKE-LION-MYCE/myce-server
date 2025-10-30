package com.myce.expo.service.impl;

import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.expo.dto.TicketQuantityRequest;
import com.myce.expo.dto.TicketSummaryResponse;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.TicketRepository;
import com.myce.expo.service.TicketService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
  private final TicketRepository ticketRepository;

  @Override
  public List<TicketSummaryResponse> getTickets(Long expoId) {
            List<Ticket> tickets = ticketRepository.findByExpoIdOrderByTypeAscSaleStartDateAsc(expoId);

    return tickets.stream()
        .map(t -> TicketSummaryResponse.builder()
            .ticketId(t.getId())
            .name(t.getName())
            .type(t.getType())
            .price(t.getPrice())
            .remainingQuantity(t.getRemainingQuantity())
            .saleStartDate(t.getSaleStartDate())
            .saleEndDate(t.getSaleEndDate())
            .description(t.getDescription())
            .build()
        )
        .toList();
  }

  // 티켓 수량 동시성 확인 및 restore과 동일하게 ticketId와 quantity로 인자값 처리
  @Transactional
  @Override
  public void updateRemainingQuantity(Long ticketId, Integer quantity) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

    ticket.updateRemainingQuantity(ticket.getRemainingQuantity() - quantity);
  }
  
  @Transactional
  @Override
  public void restoreTicketQuantity(Long ticketId, Integer quantity) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

    ticket.updateRemainingQuantity(ticket.getRemainingQuantity() + quantity);
  }
}
