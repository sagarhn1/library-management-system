package com.sagar.payment.service;

public interface PaymentWebhookService {

    void handleWebhook(
            String payload,
            String signature,
            String webhookSecret
    );
}