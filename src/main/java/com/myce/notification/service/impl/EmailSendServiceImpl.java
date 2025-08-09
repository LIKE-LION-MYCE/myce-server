package com.myce.notification.service.impl;

import com.myce.notification.service.EmailSendService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendServiceImpl implements EmailSendService {

    private final JavaMailSender mailSender;

    public void sendMail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch(MessagingException me) {
            log.error("Fail to send email. to={}, subject={}", to, subject, me);
        }
    }

}
