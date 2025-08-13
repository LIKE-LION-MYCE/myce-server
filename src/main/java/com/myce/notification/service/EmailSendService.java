package com.myce.notification.service;

import java.util.List;

public interface EmailSendService {
    void sendMail(String to, String subject, String body);
    void sendMailToMultiple(List<String> recipients, String subject, String content);
}