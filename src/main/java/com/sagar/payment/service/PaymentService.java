package com.sagar.payment.service;

import com.sagar.payment.payload.request.CreatePaymentRequest;
import com.sagar.payment.payload.request.PaymentFailedRequest;
import com.sagar.payment.payload.request.VerifyPaymentRequest;
import com.sagar.payment.payload.response.CreatePaymentResponse;

public interface PaymentService {

    CreatePaymentResponse createPayment(
            CreatePaymentRequest request,
            String userEmail
    );

    void verifyPayment(
            VerifyPaymentRequest request,
            String userEmail
    );

    void markPaymentAsFailed(
            PaymentFailedRequest request,
            String userEmail
    );
}