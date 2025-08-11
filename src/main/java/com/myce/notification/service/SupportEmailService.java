package com.myce.notification.service;

public interface SupportEmailService {
    void sendSupportMail(String to, String subject, String body);
}