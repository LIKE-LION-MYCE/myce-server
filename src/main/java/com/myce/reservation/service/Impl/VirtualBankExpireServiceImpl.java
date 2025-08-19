package com.myce.reservation.service.impl;

import com.myce.expo.service.TicketService;
import com.myce.payment.entity.ReservationPaymentInfo;
import com.myce.payment.entity.type.PaymentStatus;
import com.myce.payment.repository.ReservationPaymentInfoRepository;
import com.myce.reservation.entity.Reservation;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.repository.ReservationRepository;
import com.myce.reservation.service.VirtualBankExpireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualBankExpireServiceImpl implements VirtualBankExpireService {
    
    private final ReservationRepository reservationRepository;
    private final ReservationPaymentInfoRepository reservationPaymentInfoRepository;
    private final TicketService ticketService;

    @Override
    @Transactional
    public void processExpiredVirtualBankReservations() {
        // 어제 자정 이전에 생성된 CONFIRMED_PENDING 상태의 예약들 조회
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.CONFIRMED_PENDING, yesterday);
        
        if (expiredReservations.isEmpty()) {
            log.info("[VirtualBankExpire] 만료 처리할 가상계좌 예약이 없습니다");
            return;
        }
        
        int cancelledCount = 0;
        int paymentFailedCount = 0;
        int ticketRestoredCount = 0;
        
        for (Reservation reservation : expiredReservations) {
            try {
                processExpiredReservation(reservation);
                cancelledCount++;
                
                // 결제 정보 처리
                if (updatePaymentInfoToFailed(reservation.getId())) {
                    paymentFailedCount++;
                }
                
                // 티켓 수량 복구
                restoreTicketQuantity(reservation);
                ticketRestoredCount++;
                
                log.info("[VirtualBankExpire] 가상계좌 만료 처리 완료 - 예약ID: {}, 티켓ID: {}, 복구 수량: {}", 
                        reservation.getId(), reservation.getTicket().getId(), reservation.getQuantity());
                        
            } catch (Exception e) {
                log.error("[VirtualBankExpire] 가상계좌 만료 처리 실패 - 예약ID: {}, 오류: {}", 
                        reservation.getId(), e.getMessage(), e);
            }
        }
        
        log.info("[VirtualBankExpire] 가상계좌 만료 처리 결과 - 취소된 예약: {}건, 실패 처리된 결제: {}건, 복구된 티켓: {}건", 
                cancelledCount, paymentFailedCount, ticketRestoredCount);
    }
    
    private void processExpiredReservation(Reservation reservation) {
        reservation.updateStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }
    
    private boolean updatePaymentInfoToFailed(Long reservationId) {
        ReservationPaymentInfo paymentInfo = reservationPaymentInfoRepository.findByReservationId(reservationId)
                .orElse(null);
        
        if (paymentInfo != null && paymentInfo.getStatus() == PaymentStatus.PENDING) {
            paymentInfo.setStatus(PaymentStatus.FAILED);
            reservationPaymentInfoRepository.save(paymentInfo);
            return true;
        }
        return false;
    }
    
    private void restoreTicketQuantity(Reservation reservation) {
        Integer quantityToRestore = reservation.getQuantity();
        ticketService.restoreTicketQuantity(reservation.getTicket().getId(), quantityToRestore);
    }
}