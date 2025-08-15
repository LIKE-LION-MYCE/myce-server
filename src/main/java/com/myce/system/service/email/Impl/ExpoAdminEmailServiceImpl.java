package com.myce.system.service.email.Impl;

import com.myce.auth.dto.type.LoginType;
import com.myce.common.entity.BusinessProfile;
import com.myce.common.entity.type.TargetType;
import com.myce.common.exception.CustomErrorCode;
import com.myce.common.exception.CustomException;
import com.myce.common.repository.BusinessProfileRepository;
import com.myce.expo.entity.Expo;
import com.myce.expo.repository.AdminPermissionRepository;
import com.myce.expo.repository.ExpoRepository;
import com.myce.notification.service.EmailSendService;
import com.myce.reservation.repository.ReserverRepository;
import com.myce.system.document.EmailLog;
import com.myce.system.dto.email.ExpoAdminEmailRequest;
import com.myce.system.service.email.ExpoAdminEmailService;
import com.myce.system.service.email.mapper.ExpoAdminEmailMapper;
import com.myce.system.repository.EmailLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpoAdminEmailServiceImpl implements ExpoAdminEmailService {

    private final ExpoRepository expoRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final AdminPermissionRepository adminPermissionRepository;

    private final ReserverRepository reserverRepository;
    private final EmailLogRepository emailLogRepository;
    private final EmailSendService emailSendService;
    private final SpringTemplateEngine templateEngine;
    private final ExpoAdminEmailMapper mapper;
    
    //TODO : 추후 링크 교체, 또는 @Value로 값 주입
    private final String TERMS_URL= "http://www.myce.live";
    private final String REFUND_URL = "http://www.myce.live";
    private final String PRIVACY_URL = "http://www.myce.live";

    @Override
    @Transactional
    public void sendMail(Long memberId,
                         LoginType loginType,
                         Long expoId,
                         ExpoAdminEmailRequest dto,
                         String entranceStatus,
                         String name,
                         String phone,
                         String reservationCode,
                         String ticketName) {

        validateMyAccess(expoId, memberId, loginType);
        String html = renderEmailHtml(expoId,dto);

        List<EmailLog.RecipientInfo> recipientInfos;
        if(dto.isSelectAllMatching()){
            recipientInfos = reserverRepository.findReserversByFilter(expoId, entranceStatus, name, phone, reservationCode, ticketName)
                    .stream()
                    .map(r -> new EmailLog.RecipientInfo(r.getEmail(),r.getName()))
                    .toList();
        }else{
            recipientInfos = dto.getRecipientInfos();
        }

        List<String> emails = recipientInfos.stream()
                        .map(EmailLog.RecipientInfo::getEmail)
                        .toList();
        
        emailSendService.sendMailToMultiple(emails, dto.getSubject(), html); //TODO: 추후 대량 이메일 전송 대비 배치 도입 고려

        emailLogRepository.save(mapper.toDocument(expoId,dto,recipientInfos));
    }

    private String renderEmailHtml(Long expoId, ExpoAdminEmailRequest dto){
        String expoName = expoRepository.findById(expoId)
                .map(Expo::getTitle)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        Optional<BusinessProfile> profile = businessProfileRepository
                .findByTargetIdAndTargetType(expoId,TargetType.EXPO);

        String contactPhone = profile.map(BusinessProfile::getContactPhone).orElse(null);
        String contactEmail = profile.map(BusinessProfile::getContactEmail).orElse(null);

        String contentHtml = Optional.ofNullable(dto.getContent())
                .map(s -> s.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br/>"))
                .orElse("");

        Context ctx = new Context(Locale.KOREA);
        ctx.setVariable("preheader", toPreheader(dto.getContent(), 80));
        ctx.setVariable("subject", dto.getSubject());
        ctx.setVariable("content", contentHtml);
        ctx.setVariable("expoName",expoName);
        ctx.setVariable("contactPhone",contactPhone);
        ctx.setVariable("contactEmail",contactEmail);
        ctx.setVariable("termsUrl", TERMS_URL);
        ctx.setVariable("refundUrl", REFUND_URL);
        ctx.setVariable("privacyUrl", PRIVACY_URL);

        return templateEngine.process("mail/mail-basic",ctx);
    }

    private String toPreheader(String html, int maxLen) {
        if (html == null) return "";
        String text = html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        return text.length() > maxLen ? text.substring(0, maxLen) + "…" : text;
    }

    //TODO: 1차 구현 이후 유틸메소드화(중복 코드들 제거 및 유지보수 용이하도록) 및 권한 로직 수정
    private void validateMyAccess(Long expoId, Long memberId, LoginType loginType) {
        if(memberId == null || loginType == null){
            throw new CustomException(CustomErrorCode.MEMBER_NOT_EXIST);
        }

        switch(loginType){
            case MEMBER -> {
                if (!expoRepository.existsByIdAndMemberId(expoId, memberId)) {
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            case ADMIN_CODE -> {
                if(!adminPermissionRepository.existsByAdminCodeIdAndAdminCodeExpoIdAndIsReserverListViewTrue(memberId, expoId)){
                    throw new CustomException(CustomErrorCode.EXPO_ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_LOGIN_TYPE);
        }
    }
}