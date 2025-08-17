package com.myce.reservation.service.Impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.permission.ExpoAdminAccessValidate;
import com.myce.common.permission.ExpoAdminPermission;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.TicketRepository;
import com.myce.reservation.dto.ExpoAdminReservationResponse;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.reservation.service.ExpoAdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminReservationServiceImpl implements ExpoAdminReservationService {

    private final ExpoAdminAccessValidate expoAdminAccessValidate;
    private final TicketRepository ticketRepository;
    private final ReserverRepository reserverRepository;

    @Override
    public List<String> getExpoTicketNames(Long expoId, Long memberId, LoginType loginType) {
        expoAdminAccessValidate.ensureViewable(expoId,memberId,loginType, ExpoAdminPermission.RESERVER_LIST_VIEW);
        List<Ticket> tickets = ticketRepository.findByExpoId(expoId);

        return tickets.stream().map(Ticket::getName).toList();
    }

    @Override
    public Page<ExpoAdminReservationResponse> getMyExpoReservations(
            Long expoId, Long memberId, LoginType loginType,
            String entranceStatus, String name, String phone, String reservationCode, String ticketName,
            Pageable pageable) {

        expoAdminAccessValidate.ensureViewable(expoId,memberId,loginType, ExpoAdminPermission.RESERVER_LIST_VIEW);

        return reserverRepository.findAllResponsesByExpoIdAndStatus(
                expoId, entranceStatus, name, phone, reservationCode, ticketName, pageable);
    }
}