package com.sagar.payment.controller;

import com.sagar.payment.payload.request.CreatePaymentRequest;
import com.sagar.payment.payload.request.PaymentFailedRequest;
import com.sagar.payment.payload.request.VerifyPaymentRequest;
import com.sagar.payment.payload.response.CreatePaymentResponse;
import com.sagar.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                paymentService.createPayment(
                        request,
                        authentication.getName()
                )
        );
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<String> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication
    ) {

        paymentService.verifyPayment(
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                "Payment verified and book access granted successfully"
        );
    }

    @PostMapping("/failed")
    public ResponseEntity<String> markPaymentAsFailed(
            @Valid
            @RequestBody PaymentFailedRequest request,
            Authentication authentication
    ) {

        paymentService.markPaymentAsFailed(
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                "Payment marked as failed"
        );
    }
}