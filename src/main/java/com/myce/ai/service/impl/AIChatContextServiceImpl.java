package com.myce.ai.service.impl;

import com.myce.ai.context.PublicContext;
import com.myce.ai.context.UserContext;
import com.myce.ai.service.AIChatContextService;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.ExpoRepository;
import com.myce.member.entity.Member;
import com.myce.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatContextServiceImpl implements AIChatContextService {

    private static final String PLATFORM_ROOM_PREFIX = "platform-";

    private final MemberRepository memberRepository;
    private final ExpoRepository expoRepository;
    private final com.myce.expo.repository.TicketRepository ticketRepository;

    /**
     * 사용자별 컨텍스트 구성 (격리된 정보만 제공)
     */
    public UserContext buildUserContext(String roomCode) {
        try {
            // platform-{memberId}에서 memberId 추출
            Long userId = extractUserIdFromRoomCode(roomCode);

            // 사용자 기본 정보 조회
            Member user = memberRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                return new UserContext("사용자", "일반", List.of(), "정보 없음", userId);
            }

            // TODO: 예약 정보, 결제 상태 등은 추후 구현
            List<String> recentReservations = List.of("예약 정보 조회 예정");
            String paymentStatus = "결제 상태 조회 예정";

            return new UserContext(
                    user.getName(),
                    user.getMemberGrade() != null ? user.getMemberGrade().getGradeCode().getName() : "일반",
                    recentReservations,
                    paymentStatus,
                    userId
            );

        } catch (Exception e) {
            log.warn("사용자 컨텍스트 구성 실패 - roomCode: {}", roomCode, e);
            return new UserContext("사용자", "일반", List.of(), "정보 없음", -1L);
        }
    }

    /**
     * 공개 플랫폼 정보 구성
     */
    public PublicContext buildPublicContext() {
        try {
            // 공개 박람회 목록 조회 (상위 5개)
            List<Expo> publicExpos = expoRepository.findTop5ByOrderByCreatedAtDesc();

            // 박람회 기본 정보
            List<String> expoTitles = publicExpos.stream()
                    .map(Expo::getTitle)
                    .collect(Collectors.toList());

            // 박람회별 위치 정보 구성
            StringBuilder locationInfo = new StringBuilder();
            locationInfo.append("\n박람회 위치 정보:\n");
            for (Expo expo : publicExpos) {
                locationInfo.append(String.format("• %s: %s %s\n",
                        expo.getTitle(),
                        expo.getLocation(),
                        expo.getLocationDetail() != null ? expo.getLocationDetail() : ""
                ));
            }

            // 박람회별 티켓 정보 구성
            StringBuilder ticketInfo = new StringBuilder();
            ticketInfo.append("\n티켓 정보:\n");
            for (Expo expo : publicExpos) {
                try {
                    List<com.myce.expo.entity.Ticket> tickets = ticketRepository.findByExpoId(expo.getId());
                    if (!tickets.isEmpty()) {
                        ticketInfo.append(String.format("• %s:\n", expo.getTitle()));
                        for (com.myce.expo.entity.Ticket ticket : tickets) {
                            String remainingStatus = ticket.getRemainingQuantity() <= 0 ? " (매진)" :
                                    ticket.getRemainingQuantity() < 10 ? String.format(" (잔여 %d매)", ticket.getRemainingQuantity()) :
                                            " (예약 가능)";

                            ticketInfo.append(String.format("  - %s: %,d원 (판매: %s~%s)%s\n",
                                    ticket.getName(),
                                    ticket.getPrice(),
                                    ticket.getSaleStartDate(),
                                    ticket.getSaleEndDate(),
                                    remainingStatus
                            ));
                        }
                    } else {
                        ticketInfo.append(String.format("• %s: 티켓 정보 준비 중\n", expo.getTitle()));
                    }
                } catch (Exception e) {
                    ticketInfo.append(String.format("• %s: 티켓 정보 조회 실패\n", expo.getTitle()));
                }
            }

            String platformInfo = """
                MYCE는 박람회 관리 플랫폼입니다.
                - 박람회 예약 및 관리
                - 티켓 구매 시스템
                - 실시간 채팅 상담
                """;

            String pricingInfo = "요금제 정보는 개별 박람회마다 상이합니다.";

            // 확장된 정보를 availableExpos에 통합
            String enhancedExpoInfo = String.join(", ", expoTitles) +
                    locationInfo +
                    ticketInfo;

            return new PublicContext(List.of(enhancedExpoInfo), platformInfo, pricingInfo);

        } catch (Exception e) {
            log.warn("공개 컨텍스트 구성 실패", e);
            return new PublicContext(List.of(), "플랫폼 정보 로딩 실패", "요금 정보 조회 불가");
        }
    }

    /**
     * roomCode에서 사용자 ID 추출
     */
    private Long extractUserIdFromRoomCode(String roomCode) {
        try {
            if (roomCode != null && roomCode.startsWith(PLATFORM_ROOM_PREFIX)) {
                String[] parts = roomCode.split("-");
                if (parts.length == 2) {
                    return Long.parseLong(parts[1]);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("roomCode에서 사용자 ID 추출 실패: {}", roomCode);
        }
        throw new IllegalArgumentException("Invalid platform room code: " + roomCode);
    }
}
