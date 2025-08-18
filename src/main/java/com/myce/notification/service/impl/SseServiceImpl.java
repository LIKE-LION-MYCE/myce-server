package com.myce.notification.service.impl;

import com.myce.notification.repository.EmitterRepository;
import com.myce.notification.service.SseService;
import com.myce.reservation.entity.code.ReservationStatus;
import com.myce.reservation.entity.code.UserType;
import com.myce.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import org.apache.catalina.connector.ClientAbortException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseServiceImpl implements SseService {
    private final EmitterRepository emitterRepository;
    private final ReservationRepository reservationRepository;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 3;

    public SseEmitter subscribe(Long memberId) {
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(emitterId, sseEmitter);

        sseEmitter.onCompletion(() -> {
            log.info("sseEmitter completion, emitterId: {}", emitterId);
            emitterRepository.removeSseEmitter(emitterId);
        });

        sseEmitter.onTimeout(() -> {
            log.info("sseEmitter timeout, emitterId: {}", emitterId);
            emitterRepository.removeSseEmitter(emitterId);
        });

        sendMessage(sseEmitter, "SSE connected, emitterId: " + emitterId);

        return sseEmitter;
    }

    public void notifyToExpoClient(Long expoId, String content) {
        List<Long> allReservationMemberId = reservationRepository
                .findAllMemberIdByExpoIdAndStatusAndUserType(
                        expoId, ReservationStatus.CONFIRMED, UserType.MEMBER
                );
        log.info("send notice to Expo Client: {}", allReservationMemberId);
        for (Long id : allReservationMemberId) {
            notifyMemberViaSseEmitters(id, content);
        }
    }

    public void notifyMemberViaSseEmitters(Long memberId, String content) {
        List<SseEmitter> emitters = emitterRepository
                .findAllSseEmitterByMemberId(String.valueOf(memberId));
        emitters.forEach(sseEmitter -> {
            sendMessage(sseEmitter, content);
        });
    }

    private void sendMessage(SseEmitter sseEmitter, String content) {
        try {
            sseEmitter.send(SseEmitter.event()
                    .data(content));
        } catch (ClientAbortException e) {
            // 클라이언트 연결 중단 - 정상적인 상황, debug 레벨로 로그
            log.debug("SSE 클라이언트 연결이 중단되었습니다 (정상): {}", e.getMessage());
            sseEmitter.completeWithError(e);
        } catch (IOException e) {
            // 기타 IO 예외
            log.warn("SSE 연결 IO 오류: {}", e.getMessage());
            sseEmitter.completeWithError(e);
        } catch (Exception e) {
            // 예상하지 못한 예외
            log.error("SSE 메시지 전송 중 예상치 못한 오류 발생", e);
            sseEmitter.completeWithError(e);
        }
    }

}
