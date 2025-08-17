package com.myce.payment.service.impl;

import com.myce.common.dto.PageResponse;
import com.myce.expo.entity.Expo;
import com.myce.payment.dto.PaymentInfoResponse;
import com.myce.payment.entity.AdPaymentInfo;
import com.myce.payment.repository.AdPaymentInfoRepository;
import com.myce.payment.repository.ExpoPaymentInfoRepository;
import com.myce.payment.service.PaymentInfoPlatformService;
import com.myce.payment.service.mapper.PaymentInfoMapper;
import com.myce.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentInfoPlatformServiceImpl implements PaymentInfoPlatformService {
    private final ExpoPaymentInfoRepository expoPaymentInfoRepository;
    private final ReservationRepository reservationRepository;
    private final AdPaymentInfoRepository adPaymentInfoRepository;

    @Override
    public PageResponse<PaymentInfoResponse> getPaymentInfoPage(Integer page, Integer size, boolean latestFirst) {
        List<PaymentInfoResponse> allPayments = fetchAndSortAllPayments(latestFirst, null);
        return paginatePayments(allPayments, page, size);
    }

    @Override
    public PageResponse<PaymentInfoResponse> filterPaymentInfoPage(Integer page, Integer size,
            boolean latestFirst,
            String keyword, String type,
            LocalDate startDate, LocalDate endDate) {
        Predicate<PaymentInfoResponse> filterPredicate = paymentInfo ->
                (keyword == null || paymentInfo.getTitle().contains(keyword)) &&
                (type == null || paymentInfo.getType().equalsIgnoreCase(type)) &&
                (startDate == null || !paymentInfo.getCreatedAt().toLocalDate().isBefore(startDate)) &&
                (endDate == null || !paymentInfo.getCreatedAt().toLocalDate().isAfter(endDate));
        List<PaymentInfoResponse> allPayments = fetchAndSortAllPayments(latestFirst, filterPredicate);
        return paginatePayments(allPayments, page, size);
    }

    public List<PaymentInfoResponse> fetchAndSortAllPayments(boolean latestFirst, Predicate<PaymentInfoResponse> filterPredicate) {
        List<PaymentInfoResponse> expoPayments = expoPaymentInfoRepository.findAll()
                .stream()
                .map(paymentInfo -> {
                    Expo expo = paymentInfo.getExpo();
                    BigDecimal commissionRate = paymentInfo.getCommissionRate();
                    BigDecimal totalRevenue = reservationRepository.sumTotalRevenueByExpoId(expo.getId());
                    BigDecimal platformRevenue = totalRevenue.multiply(commissionRate)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    return PaymentInfoMapper.expoPaymentInfoToResponse(paymentInfo, platformRevenue);
                })
                .toList();

        List<PaymentInfoResponse> adPayments = adPaymentInfoRepository.findAll()
                .stream()
                .map(PaymentInfoMapper::adPaymentInfoToResponse)
                .toList();

        Comparator<PaymentInfoResponse> comparator = Comparator.comparing(PaymentInfoResponse::getCreatedAt);
        if (latestFirst) {
            comparator = comparator.reversed();
        }

        Stream<PaymentInfoResponse> combinedStream = Stream.concat(expoPayments.stream(), adPayments.stream())
                .sorted(comparator);

        if (filterPredicate != null) {
            combinedStream = combinedStream.filter(filterPredicate);
        }

        return combinedStream.collect(Collectors.toList());
    }

    private PageResponse<PaymentInfoResponse> paginatePayments(List<PaymentInfoResponse> allPayments, Integer page, Integer size) {
        int start = page * size;
        int end = Math.min(start + size, allPayments.size());

        if (start > allPayments.size()) {
            return PageResponse.from(Page.empty(PageRequest.of(page, size)));
        }

        List<PaymentInfoResponse> pagedPayments = allPayments.subList(start, end);
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentInfoResponse> paymentPage = new PageImpl<>(pagedPayments, pageable, allPayments.size());

        return PageResponse.from(paymentPage);
    }
}