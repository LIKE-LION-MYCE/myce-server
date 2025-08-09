package com.myce.notification.service;

public interface EmailSendService {
    void sendMail(String to, String subject, String body);
}
