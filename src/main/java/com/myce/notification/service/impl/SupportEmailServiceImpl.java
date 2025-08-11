package com.myce.notification.service.impl;

import com.myce.notification.service.SupportEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportEmailServiceImpl implements SupportEmailService {

    private final JavaMailSender mailSender;

    public void sendSupportMail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom("support@myce.live", "MYCE Support");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            mailSender.send(mimeMessage);
            log.info("Support email sent successfully. from=support@myce.live, to={}, subject={}", to, subject);
        } catch (MessagingException | UnsupportedEncodingException me) {
            log.error("Failed to send support email. from=support@myce.live, to={}, subject={}", to, subject, me);
        }
    }
}