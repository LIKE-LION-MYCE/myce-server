package com.myce.member.mapper.expo;

import com.myce.common.entity.BusinessProfile;
import com.myce.expo.entity.Expo;
import com.myce.expo.entity.Ticket;
import com.myce.member.dto.expo.MemberExpoDetailResponse;
import com.myce.payment.entity.ExpoPaymentInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemberExpoDetailMapper {
    
    public MemberExpoDetailResponse toMemberExpoDetailResponse(Expo expo, 
                                                               ExpoPaymentInfo paymentInfo, 
                                                               List<Ticket> tickets,
                                                               BusinessProfile businessProfile,
                                                               String categoryName) {
        
        return MemberExpoDetailResponse.builder()
                .expoId(expo.getId())
                .title(expo.getTitle())
                .thumbnailUrl(expo.getThumbnailUrl())
                .location(expo.getLocation())
                .locationDetail(expo.getLocationDetail())
                .maxReserverCount(expo.getMaxReserverCount())
                .startDate(expo.getStartDate())
                .endDate(expo.getEndDate())
                .startTime(expo.getStartTime())
                .endTime(expo.getEndTime())
                .displayStartDate(expo.getDisplayStartDate())
                .displayEndDate(expo.getDisplayEndDate())
                .description(expo.getDescription())
                .status(expo.getStatus())
                .isPremium(determineIsPremium(paymentInfo))
                .category(categoryName)
                .memberLoginId(expo.getMember().getLoginId())
                .paymentInfo(buildPaymentInfo(paymentInfo))
                .tickets(buildTicketInfoList(tickets))
                .businessInfo(buildBusinessInfo(businessProfile))
                .build();
    }
    
    private Boolean determineIsPremium(ExpoPaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            return false;
        }
        
        // 프리미엄 등록금이 기본 등록금보다 크면 프리미엄으로 판단
        Integer deposit = paymentInfo.getDeposit();
        Integer premiumDeposit = paymentInfo.getPremiumDeposit();
        
        if (deposit == null || premiumDeposit == null) {
            return false;
        }
        
        return premiumDeposit > deposit;
    }
    
    private MemberExpoDetailResponse.PaymentInfo buildPaymentInfo(ExpoPaymentInfo paymentInfo) {
        if (paymentInfo == null) {
            return null;
        }
        
        return MemberExpoDetailResponse.PaymentInfo.builder()
                .deposit(paymentInfo.getDeposit())
                .premiumDeposit(paymentInfo.getPremiumDeposit())
                .totalAmount(paymentInfo.getTotalAmount())
                .dailyUsageFee(paymentInfo.getDailyUsageFee())
                .totalDay(paymentInfo.getTotalDay())
                .build();
    }
    
    private List<MemberExpoDetailResponse.TicketInfo> buildTicketInfoList(List<Ticket> tickets) {
        return tickets.stream()
                .map(this::buildTicketInfo)
                .toList();
    }
    
    private MemberExpoDetailResponse.TicketInfo buildTicketInfo(Ticket ticket) {
        return MemberExpoDetailResponse.TicketInfo.builder()
                .ticketId(ticket.getId())
                .name(ticket.getName())
                .price(ticket.getPrice())
                .totalQuantity(ticket.getTotalQuantity())
                .type(ticket.getType())
                .build();
    }
    
    private MemberExpoDetailResponse.BusinessInfo buildBusinessInfo(BusinessProfile businessProfile) {
        if (businessProfile == null) {
            return null;
        }
        
        return MemberExpoDetailResponse.BusinessInfo.builder()
                .companyName(businessProfile.getCompanyName())
                .ceoName(businessProfile.getCeoName())
                .address(businessProfile.getAddress())
                .contactPhone(businessProfile.getContactPhone())
                .contactEmail(businessProfile.getContactEmail())
                .businessRegistrationNumber(businessProfile.getBusinessRegistrationNumber())
                .build();
    }
}