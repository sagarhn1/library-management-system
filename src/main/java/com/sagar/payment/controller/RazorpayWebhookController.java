package com.sagar.payment.controller;

import com.sagar.payment.service.PaymentWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Razorpay-Signature")
            String signature,
            HttpServletRequest request
    ) throws Exception {

        StringBuilder payload = new StringBuilder();

        try (BufferedReader reader =
                     request.getReader()) {

            String line;

            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
        }

        paymentWebhookService.handleWebhook(
                payload.toString(),
                signature,
                webhookSecret
        );

        return ResponseEntity.ok().build();
    }
}