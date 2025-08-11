package com.myce.payment.service.webhook;

import com.myce.payment.dto.PortOneWebhookRequest;

public interface PaymentWebhookService {
    void processWebhook(PortOneWebhookRequest request);
}
