package com.myce.expo.service.impl;

import com.myce.expo.dto.TicketSummaryResponse;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.TicketRepository;
import com.myce.expo.service.TicketService;
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
            .build()
        )
        .toList();
  }
}
