package com.myce.expo.service.impl;

import com.myce.common.dto.RegistrationCompanyRequest;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.service.mapper.BusinessProfileMapper;
import com.myce.expo.dto.CongestionResponse;
import com.myce.expo.dto.ExpoCardResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.entity.Category;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import com.myce.expo.entity.Ticket;
import com.myce.expo.repository.CategoryRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.expo.service.ExpoService;
import com.myce.expo.service.mapper.ExpoMapper;
import com.myce.member.entity.Member;
import com.myce.member.repository.FavoriteRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.qrcode.repository.QrCodeRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExpoServiceImpl implements ExpoService {

    private final MemberRepository memberRepository;
    private final ExpoRepository expoRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final QrCodeRepository qrCodeRepository;
    private final ExpoMapper expoMapper;
    private final TicketRepository ticketRepository;
    private final FavoriteRepository favoriteRepository;

    @Override
    public void saveExpo(Long memberId, ExpoRegistrationRequest request) {
        // 로그인한 사용자
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        // expo 객체 생성
        Expo expo = ExpoMapper.toEntity(request, member);

        // 카테고리 추가
        for (Long categoryId : request.getCategoryIds()) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.CATEGORY_NOT_EXIST));

            ExpoCategory expoCategory = ExpoCategory.builder()
                    .category(category)
                    .expo(expo)
                    .build();

            expo.getExpoCategories().add(expoCategory);
        }

        // 박람회 등록(저장)
        Expo savedExpo = expoRepository.save(expo);

        // 등록 신청한 회사 정보 저장
        RegistrationCompanyRequest company = request.getRegistrationCompanyRequest();

        BusinessProfile businessProfile = BusinessProfileMapper.toEntity(company, TargetType.EXPO, savedExpo.getId());

        businessProfileRepository.save(businessProfile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CongestionResponse getCongestionLevel(Long expoId) {
        log.info("박람회 혼잡도 조회 시작 - 박람회 ID: {}", expoId);
        
        // 박람회 정보 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        
        // 현재 시간 기준 1시간 전부터 현재까지 입장한 인원 수 조회
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        long hourlyVisitors = qrCodeRepository.countByReserverReservationExpoIdAndUsedAtAfter(
                expoId, oneHourAgo);
        
        // 시간당 수용 인원 계산
        int hourlyCapacity = calculateHourlyCapacity(expo);
        
        log.info("박람회 혼잡도 - 박람회: {}, 현재 1시간 입장자: {}, 시간당 수용인원: {}", 
                expo.getTitle(), hourlyVisitors, hourlyCapacity);
        
        return CongestionResponse.of(expoId, expo.getTitle(),
                hourlyVisitors, hourlyCapacity);
    }

    /**
     * 시간당 수용 인원 계산
     * = 총 수용인원 / 박람회 기간(일) / 하루 운영시간
     */
    private int calculateHourlyCapacity(Expo expo) {
        // 박람회 기간 계산 (일수)
        long expoDays = expo.getStartDate().until(expo.getEndDate()).getDays() + 1;
        
        // 하루 운영 시간 계산
        long dailyHours = expo.getStartTime().until(expo.getEndTime(), java.time.temporal.ChronoUnit.HOURS);
        
        // 시간당 수용 인원 = 총 수용인원 / 총 운영시간
        long totalOperatingHours = expoDays * dailyHours;
        
        int hourlyCapacity = totalOperatingHours > 0 ? 
            (int) (expo.getMaxReserverCount() / totalOperatingHours) : expo.getMaxReserverCount();
            
        log.debug("시간당 수용인원 계산 - 박람회기간: {}일, 일일운영시간: {}시간, 총운영시간: {}시간, 시간당수용인원: {}", 
                expoDays, dailyHours, totalOperatingHours, hourlyCapacity);
                
        return hourlyCapacity;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExpoCardResponse> getExpoCards(Long memberId) {
        List<ExpoCardResponse> expoCards = new ArrayList<>();
        LocalDate today = LocalDate.now();
        // 노출 구간에 있는 것만 조회
        List<Expo> expos = expoRepository
            .findAllByDisplayStartDateLessThanOrEqualToAndDisplayEndDateGreaterThanOrEqualTo(today, today);

        for(Expo expo : expos) {
            // 남은 티켓 수 합산
            List<Ticket> tickets = ticketRepository.findByExpoId(expo.getId());
            Integer remainingTickets = 0;
            for(Ticket ticket : tickets) {
                remainingTickets += ticket.getRemainingQuantity();
            }

            // 회원일 경우에만 찜 확인 가능
            boolean isBookmark = false;
            if(memberId != null){
                isBookmark = favoriteRepository.existsByMemberIdAndExpoId(memberId, expo.getId());
            }
            expoCards.add(expoMapper.toCards(expo, remainingTickets, isBookmark));
        }
        return expoCards;
    }
}
