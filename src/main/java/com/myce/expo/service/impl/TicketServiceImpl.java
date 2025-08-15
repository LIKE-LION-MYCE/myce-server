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
    List<Ticket> tickets = ticketRepository.findByExpoId(expoId);

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

  @Transactional
  @Override
  public void updateRemainingQuantity(TicketQuantityRequest request) {
    Ticket ticket = ticketRepository.findById(request.getTicketId())
        .orElseThrow(() -> new CustomException(CustomErrorCode.TICKET_NOT_EXIST));

    ticket.updateRemainingQuantity(ticket.getRemainingQuantity() - request.getQuantity());
  }
}
