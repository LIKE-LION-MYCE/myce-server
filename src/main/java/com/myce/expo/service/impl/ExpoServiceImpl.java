package com.myce.expo.service.impl;

import com.myce.common.dto.RegistrationCompanyRequest;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.common.service.mapper.BusinessProfileMapper;
import com.myce.expo.dto.*;
import com.myce.expo.dto.CongestionResponse;
import com.myce.expo.dto.ExpoCardResponse;
import com.myce.expo.dto.ExpoRegistrationRequest;
import com.myce.expo.entity.Category;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.ExpoCategory;
import com.myce.expo.entity.Review;
import com.myce.expo.entity.Ticket;
import com.myce.expo.entity.Booth;
import com.myce.expo.repository.BoothRepository;
import com.myce.expo.service.mapper.BoothMapper;
import com.myce.expo.entity.type.ExpoStatus;
import com.myce.expo.repository.CategoryRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.expo.repository.ReviewRepository;
import com.myce.expo.repository.TicketRepository;
import com.myce.expo.service.ExpoService;
import com.myce.expo.service.mapper.ExpoMapper;
import com.myce.member.entity.Member;
import com.myce.member.repository.FavoriteRepository;
import com.myce.member.repository.MemberRepository;
import com.myce.qrcode.repository.QrCodeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

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
    private final TicketRepository ticketRepository;
    private final FavoriteRepository favoriteRepository;
    private final BoothRepository boothRepository;
    private final ReviewRepository reviewRepository;
    private final BoothMapper boothMapper;

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
        int hourlyCapacity = calculateHourlyCapacityByTicketsToday(expo.getId());
        
        log.info("박람회 혼잡도 - 박람회: {}, 현재 1시간 입장자: {}, 시간당 수용인원: {}", 
                expo.getTitle(), hourlyVisitors, hourlyCapacity);
        
        return CongestionResponse.of(expoId, expo.getTitle(),
                hourlyVisitors, hourlyCapacity);
    }

    /**
     * 시간당 수용 인원 계산
     * = 총 수용인원 / 박람회 기간(일) / 하루 운영시간
     */
    public int calculateHourlyCapacityByTicketsToday(Long expoId) {
        LocalDate today = LocalDate.now();

        Expo expo = expoRepository.findById( expoId )
                .orElseThrow( () -> new CustomException( CustomErrorCode.EXPO_NOT_FOUND ) );

        // 오늘이 엑스포 개최기간 밖이면 0
        if (today.isBefore( expo.getStartDate() ) || today.isAfter( expo.getEndDate() )) {
            log.debug( "오늘({})은 엑스포 개최기간 밖 - 0 반환", today );
            return 0;
        }

        // ── (헬퍼 없이) 운영시간 계산 시작 ─────────────────────────────
        LocalTime start = expo.getStartTime();
        LocalTime end = expo.getEndTime();
        long operatingHours = ChronoUnit.HOURS.between( start, end );
        if (operatingHours <= 0) {
            operatingHours = ChronoUnit.HOURS.between( start, end.plusHours( 24 ) );
        }
        if (operatingHours <= 0) operatingHours = 1; // 안전장치

        // today가 사용기간에 포함되는 티켓 조회 (양끝 포함)
        // 예: between inclusive
        var tickets = ticketRepository.findAllByExpoIdAndDateContains( expoId, today );

        BigDecimal dailyTotal = BigDecimal.ZERO;

        for (Ticket t : tickets) {
            LocalDate s = t.getUseStartDate();
            LocalDate e = t.getUseEndDate();
            if (s == null || e == null) continue;

            long days = ChronoUnit.DAYS.between( s, e ) + 1; // +1: 양끝 포함
            if (days <= 0) continue;

            long total = Math.max( 0, t.getTotalQuantity() ); // 음수 방어
            if (total == 0) continue;

            // 티켓별 1일 인원 = total / days (소수 유지)
            BigDecimal perDay = BigDecimal.valueOf( total ).divide( BigDecimal.valueOf( days ), 8, RoundingMode.HALF_UP );

            dailyTotal = dailyTotal.add( perDay );
        }

        BigDecimal hourly = dailyTotal.divide( BigDecimal.valueOf( operatingHours ), 8, RoundingMode.HALF_UP );
        int hourlyCapacity = hourly.setScale( 0, RoundingMode.CEILING ).intValue();

        log.debug( "시간당 적정인원(티켓 total 기반) - today:{}, 티켓수:{}, 1일합계:{}, 운영시간:{}h, 결과:{}", today, tickets.size(), dailyTotal, operatingHours, hourlyCapacity );

        return Math.max( hourlyCapacity, 0 );
    }



    @Transactional(readOnly = true)
    @Override
    public Page<ExpoCardResponse> getExpoCardsFiltered(Long memberId,
        String categoryName,
        String status,
        LocalDate from,
        LocalDate to,
        String keyword,
        Pageable pageable) {

        Long categoryId = null;
        if (categoryName != null && !categoryName.isBlank()) {
            Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(()-> new CustomException(CustomErrorCode.CATEGORY_NOT_EXIST));
            if (category != null) {
                categoryId = category.getId();
            }
        }

        String keyWord = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        
        // status 파라미터 처리 - null이면 PUBLISHED와 PENDING_PUBLISH 모두 조회
        ExpoStatus expoStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                expoStatus = ExpoStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status parameter: {}, using null for all statuses", status);
                expoStatus = null;
            }
        }

        Page<Expo> exposPage = expoRepository.findPublishedExposFiltered(
            expoStatus,
            categoryId,
            keyWord,
            from,
            to,
            pageable
        );

        final List<Long> finalBookmarkedExpoIds; // Declare as final
        if (memberId != null) {
            finalBookmarkedExpoIds = favoriteRepository.findExpoIdsByMemberId(memberId);
        } else {
            finalBookmarkedExpoIds = new ArrayList<>(); // Initialize if memberId is null
        }

        return exposPage.map(expo -> {
            // 남은 티켓 수 합산
            List<Ticket> tickets = ticketRepository.findByExpoId(expo.getId());
            int remainingTickets = 0;
            for(Ticket ticket : tickets) {
                remainingTickets += ticket.getRemainingQuantity();
            }

            // 북마크된 엑스포 중에 있는지 확인
            boolean isBookmark = finalBookmarkedExpoIds.contains(expo.getId());
            return ExpoMapper.toCards(expo, remainingTickets, isBookmark);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ExpoBasicResponse getExpoBasicInfo(Long expoId) {
        log.info("박람회 기본 정보 조회 - 박람회 ID: {}", expoId);

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (expo.getStatus() == ExpoStatus.PENDING_APPROVAL || expo.getStatus() == ExpoStatus.PENDING_PAYMENT) {
            throw new CustomException(CustomErrorCode.EXPO_NOT_PUBLISHED);
        }

        // 사업자 정보 조회
        BusinessProfile businessProfile = businessProfileRepository
                .findByTargetIdAndTargetType(expoId, TargetType.EXPO)
                .orElse(null);
        

        // 카테고리 목록 조회
        List<String> categories = expo.getExpoCategories().stream()
                .map(expoCategory -> expoCategory.getCategory().getName())
                .collect(Collectors.toList());

        // 현재 예약자 수 계산 (총 발행 수량 - 남은 티켓 수)
        List<Ticket> tickets = ticketRepository.findByExpoIdOrderByCreatedAtAsc(expoId);
        int currentReservationCount = tickets.stream()
                .mapToInt(ticket -> ticket.getTotalQuantity() - ticket.getRemainingQuantity())
                .sum();

        // 주최자 상세 정보 빌드
        ExpoBasicResponse.OrganizerInfo organizerInfo = null;
        if (businessProfile != null) {
            organizerInfo = ExpoBasicResponse.OrganizerInfo.builder()
                    .companyName(businessProfile.getCompanyName())
                    .ceoName(businessProfile.getCeoName())
                    .contactPhone(businessProfile.getContactPhone())
                    .contactEmail(businessProfile.getContactEmail())
                    .address(businessProfile.getAddress())
                    .businessRegistrationNumber(businessProfile.getBusinessRegistrationNumber())
                    .build();
        }

        return ExpoBasicResponse.builder()
                .expoId(expo.getId())
                .title(expo.getTitle())
                .description(expo.getDescription())
                .thumbnailUrl(expo.getThumbnailUrl())
                .status(expo.getStatus())
                .startDate(expo.getStartDate())
                .endDate(expo.getEndDate())
                .startTime(expo.getStartTime())
                .endTime(expo.getEndTime())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .location(expo.getLocation())
                .locationDetail(expo.getLocationDetail())
                .maxReserverCount(expo.getMaxReserverCount())
                .currentReservationCount(currentReservationCount)
                .organizerName(businessProfile != null ? businessProfile.getCeoName() : "정보 없음")
                .organizerContact(businessProfile != null ? businessProfile.getContactPhone() : "정보 없음")
                .organizerInfo(organizerInfo)
                .categories(categories)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpoBookmarkResponse getExpoBookmarkStatus(Long expoId, Long memberId) {
        log.info("박람회 찜하기 상태 조회 - 박람회 ID: {}, 사용자 ID: {}", expoId, memberId);

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        // 현재 사용자의 찜 상태 확인 (회원인 경우에만)
        boolean isBookmarked = false;
        if (memberId != null) {
            try {
                isBookmarked = favoriteRepository.existsByMember_IdAndExpo_Id(memberId, expoId);
            } catch (Exception e) {
                log.warn("찜 상태 조회 중 예외 발생 - 회원 ID: {}, 박람회 ID: {}, 에러: {}", memberId, expoId, e.getMessage());
                isBookmarked = false;
            }
        }

        return ExpoBookmarkResponse.builder()
                .expoId(expo.getId())
                .expoTitle(expo.getTitle())
                .isBookmarked(isBookmarked)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpoReviewsResponse getExpoReviews(Long expoId, int page, int size) {
        log.info("박람회 리뷰 정보 조회 - 박람회 ID: {}, 페이지: {}", expoId, page);

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        // 페이징 설정
        Pageable pageable = PageRequest.of(page, size);

        // 리뷰 목록 조회
        Page<Review> reviewPage = reviewRepository.findByExpoIdOrderByCreatedAtDesc(expoId, pageable);

        // 평균 평점 계산
        Double averageRating = reviewRepository.findAverageRatingByExpoId(expoId);
        if (averageRating == null) {
            averageRating = 0.0;
        }

        // 전체 리뷰 개수
        Long totalReviews = reviewRepository.countByExpoId(expoId);

        // 별점별 개수 계산
        Object[][] ratingCounts = reviewRepository.findRatingCountByExpoId(expoId);
        ExpoReviewsResponse.RatingSummary ratingSummary = buildRatingSummary(ratingCounts);

        // 리뷰 목록 변환
        List<ExpoReviewsResponse.ReviewInfo> reviewInfos = reviewPage.getContent().stream()
                .map(review -> ExpoReviewsResponse.ReviewInfo.builder()
                        .reviewId(review.getId())
                        .memberName(review.getMember().getName())
                        .title(review.getTitle())
                        .content(review.getContent())
                        .rating(review.getRating())
                        .createdAt(review.getCreatedAt())
                        .isMyReview(false)  // 조회 전용이므로 항상 false
                        .build())
                .collect(Collectors.toList());

        return ExpoReviewsResponse.builder()
                .expoId(expo.getId())
                .expoTitle(expo.getTitle())
                .averageRating(averageRating)
                .totalReviews(totalReviews.intValue())
                .ratingSummary(ratingSummary)
                .reviews(reviewInfos)
                .build();
    }

    /**
     * 별점별 개수 데이터를 RatingSummary로 변환
     */
    private ExpoReviewsResponse.RatingSummary buildRatingSummary(Object[][] ratingCounts) {
        int[] counts = new int[6]; // 인덱스 1~5 사용 (0은 무시)

        for (Object[] row : ratingCounts) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            if (rating >= 1 && rating <= 5) {
                counts[rating] = count.intValue();
            }
        }

        return ExpoReviewsResponse.RatingSummary.builder()
                .fiveStars(counts[5])
                .fourStars(counts[4])
                .threeStars(counts[3])
                .twoStars(counts[2])
                .oneStars(counts[1])
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpoLocationResponse getExpoLocation(Long expoId) {
        log.info("박람회 위치 정보 조회 - 박람회 ID: {}", expoId);

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (expo.getStatus() != ExpoStatus.PUBLISHED) {
            throw new CustomException(CustomErrorCode.EXPO_NOT_PUBLISHED);
        }

        return ExpoLocationResponse.builder()
                .expoId(expo.getId())
                .expoTitle(expo.getTitle())
                .location(expo.getLocation())
                .locationDetail(expo.getLocationDetail())
                .latitude(expo.getLatitude())
                .longitude(expo.getLongitude())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoothResponse> getExpoBooths(Long expoId) {
        log.info("박람회 부스 정보 조회 - 박람회 ID: {}", expoId);

        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        if (expo.getStatus() != ExpoStatus.PUBLISHED) {
            throw new CustomException(CustomErrorCode.EXPO_NOT_PUBLISHED);
        }

                List<Booth> booths = boothRepository.findByExpoIdSorted(expoId);

        return booths.stream()
                .map(boothMapper::toResponse)
                .collect(Collectors.toList());
    }
}