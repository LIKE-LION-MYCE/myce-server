package com.myce.notification.service.impl;

import com.myce.notification.service.EmailSendService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendServiceImpl implements EmailSendService {

    private final JavaMailSender mailSender;

    public void sendMail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom("noreply@myce.live", "MYCE");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            mailSender.send(mimeMessage);
            log.info("System email sent successfully. from=noreply@myce.live, to={}, subject={}", to, subject);
        } catch (MessagingException | UnsupportedEncodingException me) {
            log.error("Failed to send system email. from=noreply@myce.live, to={}, subject={}", to, subject, me);
        }
    }

    public void sendMailToMultiple(List<String> recipients, String subject, String content){
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom("noreply@myce.live", "MYCE");

            String[] toArray = recipients.toArray(new String[0]);
            messageHelper.setBcc(toArray);

            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully. from=noreply@myce.live, recipients={}명, subject={}", recipients.size(), subject);
        } catch (MessagingException | UnsupportedEncodingException me) {
            log.error("Failed to send email. recipients={}명, subject={}", recipients.size(), subject);
        }
    }
}
