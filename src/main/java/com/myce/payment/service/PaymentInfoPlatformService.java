package com.myce.payment.service;

import com.myce.common.dto.PageResponse;
import com.myce.payment.dto.PaymentInfoResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

public interface PaymentInfoPlatformService {
    PageResponse<PaymentInfoResponse> getPaymentInfoPage(Integer page, Integer size,
                                                         boolean latestFirst);

    PageResponse<PaymentInfoResponse> filterPaymentInfoPage(Integer page, Integer size,
                                                            boolean latestFirst, String status, String keyword, LocalDate startDate, LocalDate endDate);

    List<PaymentInfoResponse> fetchAndSortAllPayments(boolean latestFirst,
                                                      Predicate<PaymentInfoResponse> filterPredicate);
}